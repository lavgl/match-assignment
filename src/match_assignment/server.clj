(ns match-assignment.server
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [org.httpkit.server :as http]
            [reitit.ring :as ring]))


(defn make-app []
  (let [routes [["/" {:get {:handler (fn [_]
                                       {:status 200
                                        :body   "hello world!"})}}]]]
    (-> (ring/router routes)
        (ring/ring-handler))))


(mount/defstate server
  :start (let [port   5000 ;; TODO: move to config
               server (http/run-server (make-app) {:port port})]
           (log/info "Started http server on" port)
           server)
  :stop (do
          (log/info "Stopping http server")
          (server)))
