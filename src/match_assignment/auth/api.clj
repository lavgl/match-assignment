(ns match-assignment.auth.api
  (:require [com.akovantsev.blet.core :refer [blet blet!]]

            [match-assignment.auth.core :as auth]
            [match-assignment.auth.dal :as auth.dal]
            [match-assignment.auth.utils :as auth.utils]))


(def UserLogin
  [:map
   [:username string?]
   [:password string?]])


(defn registration [req]
  (blet [{:keys [username password]} (-> req :body-params)
         is-username-valid           (auth.utils/username-valid? username)
         is-password-valid           (auth.utils/password-valid? password)
         is-username-taken           (auth.dal/username-taken? username)
         password-hash               (auth.utils/hash-password password)]

    (cond
      is-username-taken
      {:status 400
       :body   {:error "Username is already taken"}}

      (not is-username-valid)
      {:status 400
       :body   {:error "Username is invalid"}}

      (not is-password-valid)
      {:status 400
       :body   {:error "Password is invalid"}}

      :else
      (do
        (auth.dal/create-user! username password-hash)
        {:status 201}))))


(defn login [req]
  (blet [{:keys [username password]} (-> req :body-params)
         is-username-valid           (auth.utils/username-valid? username)
         user                        (auth.dal/user-by-username username)
         is-password-correct         (auth.utils/verify-password password (:password_hash user))
         token                       (auth/make-token user)]
    (cond
      (or (not is-username-valid)
          (nil? user))
      {:status 400
       :body   {:error "The username or password is incorrect"}}

      is-password-correct
      {:status 200
       :body   {:token token}}

      :else
      {:status 400
       :body   {:error "The username or password is incorrect"}})))
