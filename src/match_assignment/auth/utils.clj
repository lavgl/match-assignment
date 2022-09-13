(ns match-assignment.auth.utils
  (:require [buddy.hashers :as hashers]))

(defn hash-password [password]
  (hashers/derive password {:alg :argon2id}))


(defn verify-password [attempt encrypted]
  (:valid (hashers/verify attempt encrypted)))


(defn username-valid? [username]
  (boolean (re-matches #"^\w{3,64}$" username)))


(defn password-valid? [password]
  (boolean (re-matches #"^[\w!$^]{5,64}$" password)))
