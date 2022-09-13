(ns match-assignment.auth.core
  (:require [buddy.sign.jwt :as jwt]
            [buddy.auth.backends :as buddy-auth-backends]
            [buddy.auth.middleware :as buddy-auth-middleware]))


;; TODO: get private key from env param / file via config
(def private-key "private key")


(def token-backend
  (buddy-auth-backends/jws {:secret     private-key
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
    (jwt/sign {:user_id user-id} private-key {:alg :hs512})))
