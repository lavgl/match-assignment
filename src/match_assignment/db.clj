(ns match-assignment.db
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as jdbc-rs]
            [honey.sql :as sql]

            [match-assignment.config :as config]))


(mount/defstate conn
  :start (let [path    (:path (config/db))
               db-spec {:dbtype "sqlite"
                        :dbname path}]
           (log/info "init db: " path)
           (jdbc/get-datasource db-spec)))


(defn query->args [query]
  (if (string? query)
    [query]
    (sql/format query)))


(defn q [query]
  (jdbc/execute! conn (query->args query)
    {:builder-fn jdbc-rs/as-unqualified-lower-maps}))


(defn one [query]
  (jdbc/execute-one! conn (query->args query)
    {:builder-fn jdbc-rs/as-unqualified-lower-maps}))
