(ns match-assignment.auth.core
  (:require [buddy.sign.jwt :as jwt]))

(defn make-token [user]
  (let [user-id (:id user)]
    ;; TODO: get private key from env param / file via config
    (jwt/sign {:user_id user-id} "private key" {:alg :hs512})))
