(ns match-assignment.server
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [org.httpkit.server :as http]
            [reitit.ring :as ring]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [reitit.ring.middleware.exception :as exception]
            [reitit.ring.coercion :as coercion]
            [reitit.coercion.malli]
            [muuntaja.core :as m]
            [malli.util :as mu]

            [match-assignment.auth.core :as auth]
            [match-assignment.auth.api :as auth.api]
            [match-assignment.maze.api :as maze.api]))


(def ErrorMessage
  [:map
   [:error string?]])


(defn make-app []
  (let [routes [["/swagger.json" {:get {:no-doc  true
                                        :handler (swagger/create-swagger-handler)}}]
                ["/user" {:post {:handler    auth.api/registration
                                 :swagger    {:tags ["auth"]}
                                 :parameters {:body auth.api/UserLogin}
                                 :responses  {201 {}
                                              400 {:body ErrorMessage}}}}]
                ["/login" {:post {:handler    auth.api/login
                                  :swagger    {:tags ["auth"]}
                                  :parameters {:body auth.api/UserLogin}
                                  :responses  {200 {:body {:token string?}}
                                               400 {:body ErrorMessage}}}}]

                ["" {:middleware [auth/authorized?]}
                 ["/maze" {:get  {:handler maze.api/list
                                  :swagger {:tags ["maze"]}}
                           :post {:handler    maze.api/create
                                  :swagger    {:tags ["maze"]}
                                  :parameters {:body maze.api/MazeInput}
                                  :responses  {201 {:body {:id int?}}
                                               400 {:body ErrorMessage}}}}]
                 ["/maze/:maze-id/solution" {:get {:handler    maze.api/solution
                                                   :swagger    {:tags ["maze"]}
                                                   :parameters {:path  [:map
                                                                        [:maze-id int?]]
                                                                :query [:map
                                                                        [:steps [:enum "min" "max"]]]}}}]]]
        default (ring/routes
                  (swagger-ui/create-swagger-ui-handler {:path "/"})
                  (ring/create-default-handler))]
    (-> (ring/router routes {:exception pretty/exception
                             :data      {:muuntaja   m/instance
                                         :coercion   (reitit.coercion.malli/create
                                                       {:compile    mu/closed-schema
                                                        :error-keys #{:schema :errors :value :in :humanized}})
                                         :middleware [swagger/swagger-feature
                                                      parameters/parameters-middleware
                                                      muuntaja/format-negotiate-middleware
                                                      muuntaja/format-response-middleware
                                                      muuntaja/format-request-middleware
                                                      exception/exception-middleware
                                                      coercion/coerce-response-middleware
                                                      coercion/coerce-request-middleware
                                                      auth/token-middleware]}})
        (ring/ring-handler default))))


(mount/defstate server
  :start (let [port   5000 ;; TODO: move to config
               server (http/run-server (make-app) {:port port})]
           (log/info "Started http server on" port)
           server)
  :stop (do
          (log/info "Stopping http server")
          (server)))
