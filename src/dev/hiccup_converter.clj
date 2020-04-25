(ns hiccup-converter
  (:use [clojure.data.xml :only (parse)])
  (:use clojure.pprint)
  (:import (java.io File)))

(defn format-attrs
  [m]
  (when m
    (format "%s" m)))

(defn empty-when-null
  [x]
  (if (nil? x)
    ""
    x))

(declare format-full-node)

(defn format-node
  [node]
  (cond
   (string? node) (format "\"%s\"" (.trim node))
   (nil? node) nil
   :else (format-full-node node)))

(defn format-full-node
  [node]
  (format "[%s %s %s]\n"
          (:tag node)
          (empty-when-null (format-attrs (:attrs node)))
          (clojure.string/join " " (map format-node (:content node)))))

(defn transform-str
  [str]
  (->> str
       java.io.StringReader.
       parse
       format-node
       read-string
       pprint
       print))

(defn transform-file [f-name]
  (transform-str (slurp f-name)))
