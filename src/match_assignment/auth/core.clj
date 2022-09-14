(ns match-assignment.auth.core
  (:require [mount.core :as mount]
            [buddy.sign.jwt :as jwt]
            [buddy.auth.backends :as buddy-auth-backends]
            [buddy.auth.middleware :as buddy-auth-middleware]

            [match-assignment.config :as config]))


(defn- -secret [] (config/auth-secret))
(def secret (memoize -secret))


(mount/defstate token-backend
  :start (buddy-auth-backends/jws {:secret     (secret)
                                   :options    {:alg :hs512}
                                   :token-name "Bearer"}))


(defn token-middleware [handler]
  (buddy-auth-middleware/wrap-authentication handler token-backend))


(defn authorized? [handler]
  (fn [req]
    (if (some? (:identity req))
      (handler req)
      {:status 401
       :body   {:error "Unauthorized"}})))


(defn make-token [user]
  (let [user-id (:id user)]
    ;; NOTE: in the real world, tokens should live for a limited time,
    ;; but for code challenge purposes, it's simpler to have eternal tokens
    (jwt/sign {:user_id user-id} (secret) {:alg :hs512})))
