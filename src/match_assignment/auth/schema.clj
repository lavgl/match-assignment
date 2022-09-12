(ns match-assignment.auth.schema)


(def UserLogin
  [:map
   [:username string?]
   [:password string?]])


(def ErrorMessage
  [:map
   [:error string?]])
