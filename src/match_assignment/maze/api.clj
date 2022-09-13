(ns match-assignment.maze.api
  (:refer-clojure :exclude [list])
  (:require [com.akovantsev.blet.core :refer [blet blet!]]

            [match-assignment.maze.core :as maze]
            [match-assignment.maze.dal :as maze.dal]))


(def MazeInput
  [:map
   [:gridSize string?]
   [:entrance string?]
   [:walls [:vector string?]]])


(defn create [req]
  (blet! [input                           (-> req :body-params)
          {:maze/keys [error] :as result} (maze/process input)
          min-path                        (-> result ::maze/min maze/format-path)
          max-path                        (-> result ::maze/max maze/format-path)

          ;; TODO: replace user-id with real identity provided by token
          inserted-maze (maze.dal/insert-maze! {:user-id  1
                                                :input    input
                                                :min-path min-path
                                                :max-path max-path})]
    (cond
      (= error :maze/no-solutions)
      {:status 400
       :body   {:error "Maze has ho solutions"}}

      (= error :maze/too-many-exits)
      {:status 400
       :body   {:error "Maze should have one exit"}}

      :else
      {:status 201
       :body   {:id (:id inserted-maze)}})))


(defn format-list-response [mazes]
  (->> mazes
       (map #(assoc (:configuration %)
               :id (:id %)))))


(defn list [req]
  ;; TODO: get true identity
  (let [user-id 1
        mazes   (maze.dal/mazes-by-user-id user-id)]
    {:status 200
     :body   {:mazes (format-list-response mazes)}}))


(defn solution [{:keys [parameters] :as req}]
  (blet [maze-id (-> parameters :path :maze-id)
         steps   (-> parameters :query :steps)
         ;; TODO: get true identity
         user-id 1

         {:keys [min_path max_path]
          :as   solutions} (maze.dal/solutions {:user-id user-id
                                                :maze-id maze-id})]
    (cond
      (empty? solutions)
      {:status 404
       :body   {:error "Not found"}}

      :else
      {:status 200
       :body   {:path (condp = steps
                        "min" min_path
                        "max" max_path)}})))
