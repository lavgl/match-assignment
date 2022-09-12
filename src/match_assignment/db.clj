(ns match-assignment.db
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [next.jdbc :as jdbc]
            [honey.sql :as sql]))


(mount/defstate conn
  :start (let [db-spec {:dbtype "sqlite"
                        ;; TODO: move to config
                        :dbname "/Users/v.homonov/.match_assignment/database.db"}]
           (log/info "init db: " (:dbname db-spec))
           (jdbc/get-datasource db-spec)))


(defn query->args [query]
  (if (string? query)
    [query]
    (sql/format query)))


(defn q [query]
  (jdbc/execute! conn (query->args query)))
