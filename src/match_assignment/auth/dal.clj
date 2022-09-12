(ns match-assignment.auth.dal
  (:require [match-assignment.db :as db]))


(defn create-user-q [username password-hash]
  {:insert-into :auth
   :values      [{:username      username
                  :password_hash password-hash}]})


(defn create-user! [username password-hash]
  (db/q (create-user-q username password-hash)))


(defn user-by-username-q [username]
  {:from   [:auth]
   :select [:user_id]
   :where  [:= username :username]})


(defn username-taken? [username]
  (some? (db/one (user-by-username-q username))))
