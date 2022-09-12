(ns match-assignment.auth.utils
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]))


(defn hash-password [password]
  (hashers/derive password {:alg :argon2id}))


(defn verify-password [attempt encrypted]
  (:valid (hashers/verify attempt encrypted)))


(defn username-valid? [username]
  (boolean (re-matches #"^\w{3,64}$" username)))


(defn password-valid? [password]
  (boolean (re-matches #"^[\w!$^]{5,64}$" password)))


(defn make-token [user]
  (let [user-id (:auth/id user)]
    ;; TODO: get private key from env param / file via config
    (jwt/sign {:user_id user-id} "private key" {:alg :hs512})))


