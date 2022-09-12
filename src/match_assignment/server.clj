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

            [match-assignment.auth.api :as auth.api]))


(defn make-app []
  (let [routes [["/swagger.json" {:get {:no-doc  true
                                        :handler (swagger/create-swagger-handler)}}]
                ["/user" {:post {:handler    auth.api/registration
                                 :swagger    {:tags ["auth"]}
                                 :parameters {:body
                                              [:map
                                               [:username string?]
                                               [:password string?]]}
                                 :responses  {201 {}
                                              400 {:body
                                                   [:map
                                                    [:error string?]]}}}}]
                ["/login" {:post {:handler (fn [_] nil)
                                  :swagger {:tags ["auth"]}}}]

                ["/maze" {:get  {:handler (fn [_] nil)
                                 :swagger {:tags ["maze"]}}
                          :post {:handler (fn [_] nil)
                                 :swagger {:tags ["maze"]}}}]
                ["/maze/:id/solution" {:get {:handler (fn [] nil)
                                             :swagger {:tags ["maze"]}}}]]
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
                                                      coercion/coerce-request-middleware]}})
        (ring/ring-handler default))))


(mount/defstate server
  :start (let [port   5000 ;; TODO: move to config
               server (http/run-server (make-app) {:port port})]
           (log/info "Started http server on" port)
           server)
  :stop (do
          (log/info "Stopping http server")
          (server)))
