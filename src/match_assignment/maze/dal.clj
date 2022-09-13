(ns match-assignment.maze.dal
  (:refer-clojure :exclude [get])
  (:require [jsonista.core :as json]

            [match-assignment.db :as db]))


(defn insert-maze-q [{:keys [user-id input min-path max-path]}]
  {:insert-into :mazes
   :values      [{:user_id       user-id
                  :configuration (json/write-value-as-string input)
                  :min_path      (json/write-value-as-string min-path)
                  :max_path      (json/write-value-as-string max-path)}]
   :returning   [:id]})


(defn insert-maze! [params]
  (db/one (insert-maze-q params)))


(defn mazes-by-user-id-q [user-id]
  {:from   [:mazes]
   :select [:id :configuration]
   :where  [:= user-id :user_id]})


(defn mazes-by-user-id [user-id]
  (->> (db/q (mazes-by-user-id-q user-id))
       (map #(update % :configuration json/read-value))))


(defn solutions-q [{:keys [user-id maze-id]}]
  (-> {:from   [:mazes]
       :select [:id :min_path :max_path]
       :where  [:and
                [:= :id maze-id]
                [:= :user_id user-id]]}))


(defn solutions [opts]
  (when-let [solutions (db/one (solutions-q opts))]
    (-> solutions
        (update :min_path json/read-value)
        (update :max_path json/read-value))))
