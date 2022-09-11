(ns match-assignment.maze
  (:refer-clojure :exclude [print])
  (:require [clojure.string :as str]
            [clojure.set :as set]

            [com.akovantsev.blet.core :refer [blet blet!]]))


;; `parse-x` is a map {"A" 1, "B" 2, ..., "Z" 26}
(def parse-x (into {} (map vector (map str "ABCDEFGHIJKLMNOPQRSTUVWXYZ") (rest (range)))))
(def parse-y #(Integer/parseInt %))
(def unparse-x (set/map-invert parse-x))


(defn parse-gridsize [^String size]
  (let [[_ max-x max-y] (re-matches #"(\d+)x(\d+)" size)]
    (try
      [(Integer/parseInt max-x)
       (Integer/parseInt max-y)]
      (catch Exception _
        nil))))


(defn parse-cell [^String id]
  (when-let [[_ x y] (re-matches #"([a-zA-Z]+)(\d+)" id)]
    [(parse-x x) (parse-y y)]))


(defn parse [{::keys [gridsize entrance walls]}]
  (let [[max-x max-y] (parse-gridsize gridsize)
        walls         (set (map parse-cell walls))
        entrance      (parse-cell entrance)
        exit          (->> (map vector (range max-x 0 -1) (repeat max-y))
                           (remove #{entrance})
                           (remove walls)
                           first)]
    {::gridsize [max-x max-y]
     ::entrance entrance
     ::walls    walls
     ::exit     exit}))


(defn ->str [{::keys [gridsize entrance walls exit path]}]
  (let [[max-x max-y] gridsize
        path?         (set path)
        wall?         walls
        print-cell    (fn [cell]
                        (cond
                          (wall? cell)      "#"
                          (= entrance cell) "@"
                          (= exit cell)     "*"
                          (path? cell)      "."
                          :else             " "))
        xs            (range 1 (inc max-x))
        ys            (range 1 (inc max-y))
        print-line    (fn [y]
                        (->> xs
                             (map (fn [x] (print-cell [x y])))
                             (str/join)
                             (str y "|")))
        heading-line  (->> xs
                           (map unparse-x)
                           (str/join)
                           (str "  "))]
    (->> ys
         (map print-line)
         (str/join "\n")
         (str heading-line "\n"))))


(defn print [maze]
  (cond-> maze
    (nil? (::exit maze)) parse
    :always              ->str
    :always              println))


(defn neighbors [[x y] & {::keys [gridsize]}]
  (let [[max-x max-y] gridsize

        x- (dec x), x+ (inc x)
        y- (dec y), y+ (inc y)]
    (->> [(when (<= 1 y-)    [x  y-])   ;; up
          (when (<= 1 x-)    [x- y])    ;; left
          (when (<= x+ max-x) [x+ y])   ;; right
          (when (<= y+ max-y) [x  y+])] ;; down
      (remove nil?))))


(defn solutions [input]
  ;; TODO: add docstring
  (let [{::keys [gridsize entrance walls exit] :as maze} (parse input)

        step (fn step [initial-todo]
               (lazy-seq
                 (loop [todo initial-todo]
                   (blet [[seen path] (peek todo)
                          xy          (peek path)
                          todo-       (pop todo)
                          frontier    (->> (neighbors xy ::gridsize gridsize)
                                           (remove walls)
                                           (remove seen))
                          todos       (map (fn [xy]
                                             [(conj seen xy) (conj path xy)])
                                        frontier)
                          todo+       (into todo- todos)]
                     (cond
                       (empty? todo)     nil
                       (= exit xy)       (cons (assoc maze
                                                 ::path     path
                                                 ::distance (count path))
                                           (step todo-))
                       (empty? frontier) (recur todo-)
                       :else             (recur todo+))))))]
    (step [[#{} [entrance]]])))


(comment
  (def TEST-INPUT {::gridsize "8x8"
                   ::entrance "A1"
                   ::walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", #_"B5", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "D7", "G7", "B8"]})

  (time
    (->> (solutions TEST-INPUT)
         (sort-by ::distance)
         (take 1)
         (map print)))

  (time
    (->> (solutions TEST-INPUT)
         (sort-by ::distance >)
         (take 1)
         (map print))))
