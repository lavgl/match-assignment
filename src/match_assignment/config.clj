(ns match-assignment.config
  (:require [clojure.java.io :as io]
            [mount.core :as mount]
            [aero.core :as aero]))


(defn read-config []
  (-> (io/resource "config.edn")
      (aero/read-config)))


(mount/defstate config
  :start (read-config))


(defn server [] (-> config :server))
(defn db [] (-> config :db))


(defn auth-secret []
  (let [path   (-> config :auth :secret :path)
        reader (io/reader path)]
    (first (line-seq reader))))

