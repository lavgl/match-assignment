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
   :select [[:user_id :id] :username :password_hash]
   :where  [:= username :username]})


(defn user-by-username [username]
  (db/one (user-by-username-q username)))


(defn username-taken? [username]
  (some? (user-by-username username)))
