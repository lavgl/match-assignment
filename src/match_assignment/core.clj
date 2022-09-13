(ns match-assignment.core
  (:gen-class)
  (:require [mount.core :as mount]

            [match-assignment.config]
            [match-assignment.server]
            [match-assignment.db]
            [match-assignment.auth.core]))


(defn -main [& _args]
  (mount/start))
