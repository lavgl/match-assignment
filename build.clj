(ns build
  (:require [clojure.tools.build.api :as b]))


(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def uber-file (format "target/match-%s.jar" version))


(defn clean [_]
  (b/delete {:path "target"}))


(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis      basis
                  :ns-compile ['match-assignment.core]
                  :class-dir  class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :exclude   ["LICENSE"]
           :basis     basis
           :main      'match-assignment.core}))
