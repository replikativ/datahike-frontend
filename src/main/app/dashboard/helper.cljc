(ns app.dashboard.helper
  (:require
   [cljs.reader :as reader]))

;; Adapts answers such as: #{[{:db/id 10, :player/event [{:db/id 7} {:db/id 8} {:db/id 11}], :player/name Paul, :player/team [{:db/id 11} {:db/id 12}]}]}



(defn convert-map
  "In a map m, apply f to keys and vals."
  [m f]
  (into {} (map (juxt (comp f key) (comp f val))) m))


(defn clj-to-str
  "In a map m, replaces anything that are not numbers by the corresponding string. I.e. vectors would simply be their string representations."
  [m]
  (convert-map m (fn [val]
                   (if (number? val) val (str val)))))

(defn str-to-clj
  "In a map m, replaces anything that are not numbers by the corresponding string. I.e. vectors would simply be their string representations."
  [m]
  (convert-map m (fn [val]
                   ;; In case we are seeing an element which has to be a string in the end, we have to surround it with "".
                   (if (string? val)
                     (let [read-str (reader/read-string val)]
                       (println "????? " read-str "  " (type read-str))
                       (if (symbol? read-str)
                         (str read-str)
                         read-str))
                     val))))
