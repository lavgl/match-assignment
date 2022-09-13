(ns match-assignment.maze-test
  (:require [clojure.test :as t]

            [match-assignment.maze.core :as maze]))

;; maze 1: one exit, min steps == max steps
;;
;;   ABCDEFGH
;; 1|@ #   #
;; 2|# # # #
;; 3|  # #
;; 4| ## ###
;; 5| #  #
;; 6| # ## ##
;; 7| # #  #
;; 8|*#
;;
;; solution:
;;   ABCDEFGH
;; 1|@.#   #
;; 2|#.# # #
;; 3|..# #
;; 4|.## ###
;; 5|.#  #
;; 6|.# ## ##
;; 7|.# #  #
;; 8|*#
(def OK-MAZE-1 {:gridSize "8x8"
                :entrance "A1"
                :walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", "B5", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "D7", "G7", "B8"]})


;; maze 2: one exit, min steps == max steps
;;
;;   ABCDEFGH
;; 1|@ #   #
;; 2|# # # #
;; 3|  # #
;; 4| ## ###
;; 5|    #
;; 6| # ## ##
;; 7| # #  #
;; 8|#### #
;;
;; solution:
;;   ABCDEFGH
;; 1|@.#...#
;; 2|#.#.#.#
;; 3|..#.#...
;; 4|.##.###.
;; 5|....#...
;; 6| # ##.##
;; 7| # #..#
;; 8|####*#
(def OK-MAZE-2 {:gridSize "8x8"
                :entrance "A1"
                :walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "D7", "G7", "B8" "A8" "B8" "C8" "D8" "F8"]})


;; maze 3: one exit, min steps != max steps
;;
;;   ABCDEFGH
;; 1|@ #   #
;; 2|# # # #
;; 3|  # #
;; 4| ## ###
;; 5|    #
;; 6| # ## ##
;; 7| #    #
;; 8|#### #
;;
;; min solution:
;;   ABCDEFGH
;; 1|@.#   #
;; 2|#.# # #
;; 3|..# #
;; 4|.## ###
;; 5|... #
;; 6| #.## ##
;; 7| #... #
;; 8|####*#
;;
;; max solution:
;;   ABCDEFGH
;; 1|@.#...#
;; 2|#.#.#.#
;; 3|..#.#...
;; 4|.##.###.
;; 5|....#...
;; 6| # ##.##
;; 7| #  ..#
;; 8|####*#
(def OK-MAZE-3 {:gridSize "8x8"
                :entrance "A1"
                :walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "G7", "B8" "A8" "B8" "C8" "D8" "F8"]})


;; bad maze 1: two reachable exits
;;
;;   ABCDEFGH
;; 1|@ #   #
;; 2|# # # #
;; 3|  # #
;; 4| ## ###
;; 5|    #
;; 6| # ## ##
;; 7| #    #
;; 8| ### #
(def BAD-MAZE-1 {:gridSize "8x8"
                 :entrance "A1"
                 :walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "G7", "B8" "B8" "C8" "D8" "F8"]})


;; bad maze 2: no solutions
;;   ABCDEFGH
;; 1|@ #   #
;; 2|# # # #
;; 3|  # #
;; 4|### ###
;; 5|    #
;; 6| # ## ##
;; 7| #    #
;; 8| ### #
(def BAD-MAZE-2 {:gridSize "8x8"
                 :entrance "A1"
                 :walls    ["C1", "G1", "A2", "C2", "E2", "G2", "C3", "E3", "B4", "C4", "E4", "F4", "G4", "E5", "B6", "D6", "E6", "G6", "H6", "B7", "G7", "B8" "B8" "C8" "D8" "F8" "A4"]})


(def TOO-BIG-MAZE {:gridSize "30x30"
                   :entrance "A1"
                   :walls    []})


(t/deftest maze-input-validation
  (t/testing "if the maze has no solutions, error is returned"
    (t/are [error maze] (= error (-> maze maze/process ::maze/error))
      nil                 OK-MAZE-1
      nil                 OK-MAZE-2
      nil                 OK-MAZE-3
      ::maze/no-solutions BAD-MAZE-2))

  (t/testing "a maze can only have one exit point, error otherwise"
    (t/are [error maze] (= error (-> maze maze/process ::maze/error))
      nil                   OK-MAZE-1
      nil                   OK-MAZE-2
      nil                   OK-MAZE-3
      ::maze/too-many-exits BAD-MAZE-1))

  (t/testing "the maximum maze side is 26"
    (t/are [error maze] (= error (-> maze maze/process ::maze/error))
      nil OK-MAZE-1
      nil OK-MAZE-2
      nil OK-MAZE-3
      ::maze/too-big-maze TOO-BIG-MAZE)))


(t/deftest maze-solutitions-are-good
  (t/testing "maze/process returns min_path and max_path solutions"
    (let [res (maze/process OK-MAZE-1)]
      (t/is (some? (::maze/min res)))
      (t/is (some? (::maze/max res)))))

  (t/testing "min_path and max_path are equal if there is only one solution"
    (let [res (maze/process OK-MAZE-2)]
      (t/is (= (::maze/min res) (::maze/max res)))))

  (t/testing "min_path and max_path could be different if there are several solutions"
    (let [res (maze/process OK-MAZE-3)]
      (t/is (not= (::maze/min res) (::maze/max res)))))

  (t/testing "min_path is always shorter than max_path, if they have different distance"
    (let [res          (maze/process OK-MAZE-3)
          min-distance (->> res ::maze/min ::maze/distance)
          max-distance (->> res ::maze/max ::maze/distance)]
      (t/is (> max-distance min-distance)))))
