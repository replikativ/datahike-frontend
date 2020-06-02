(ns app.dashboard.ui.queries.datoms
  (:require
   [app.dashboard.mutations.datoms :as dm]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody label textarea]]
   [com.fulcrologic.fulcro.mutations :as m]
   ["material-table" :default MaterialTable]))


(declare Datoms)

;; TODO: Make datahike-server accept connection from localhost:4000

(defsc QueryInput [this {:query-input/keys [id pull-expr where-expr] :as props}]
  {:query [:query-input/id :query-input/pull-expr :query-input/where-expr]
   :initial-state (fn [_] {:query-input/id  :query-input-init-state
                           :query-input/where-expr "[?e :name 'IVan']"
                           :query-input/pull-expr  "[(pull ?e [*])]"})
   :ident         (fn [] [:query-input/id :the-query-input])}
  (div
    (div
      (label "Pull expr:"
        ;; TODO: try to use this same string preset in the resolver
        (let [set-string! (fn [event]
                            (println "---->" (type event))
                            (m/set-string! this :query-input/pull-expr :event event))]
          (textarea {:value    (or pull-expr "[(pull ?e [*])]")
                     :onChange set-string!
                     :onPaste  set-string!}))))
    (div
      (label "Where:"
        (let [set-string! #(m/set-string! this :query-input/where-expr :event %)]
          (textarea {:value (or where-expr "[?e _ _]")
                     :onPaste set-string!
                     :onChange set-string!}))))

    (div
      (println "Pull: "  pull-expr  "type: " (type pull-expr))
      (println "Where: " where-expr)
      (button {:onClick
               (fn []
                 (println "after submit: pull-expr " (type pull-expr))
                 (println "after submit: Where-Expr: " where-expr)
                 (comp/transact! this [(dm/submit-query-input
                                         {:query-input/target-comp :app.dashboard.ui.queries.datoms/Datoms
                                          :query-input/where-expr where-expr ;; "[?e :name \"IVan\"]"
                                          ;; TODO: use the var pull-expr here once we use this same string set in the resolver
                                          :query-input/pull-expr pull-expr})
                                       (comp/get-query Datoms)]))}
        "Query!"))))

(def ui-query-input (comp/factory QueryInput))




(def mtable (interop/react-factory MaterialTable))

;; TODO: look of QueryInput

;;TODO BUG: when receiveing #{[{:db/id 536870926, :db/txInstant #inst "2020-05-20T15:08:38.020-00:00"}]}
;; May be use below function to check whether val is #inst and convert it to string.

;; Adapts answers such as: #{[{:db/id 10, :player/event [{:db/id 7} {:db/id 8} {:db/id 11}], :player/name Paul, :player/team [{:db/id 11} {:db/id 12}]}]}
(defn- non-number-vals-to-str
  "In map m, replaces vals that are not numbers by the corresponding string. I.e. vectors would simply be their string representations."
  [m]
  (let [map-vals (fn [f m]
                   (into {} (map (juxt key (comp f val))) m))
        vec-to-str (fn [val]
                     (if (number? val) val (str val)))]
    (map-vals vec-to-str m)))



(defsc Datoms [this {:datoms/keys [id elements query-input] :as props}]
  {:query [:datoms/id :datoms/elements
           {:datoms/query-input (comp/get-query QueryInput)}]
   :initial-state (fn [_] {:datoms/id      ":datoms-init-state"
                           :datoms/elements {}
                           :datoms/query-input (comp/get-initial-state QueryInput)})
   :ident         (fn [] [:datoms/id :the-datoms])
   :route-segment ["datoms"]}
  ;; Expects: [[{:id 1, :attribute :age, :value 31, :transac-id 536870961, :added true}]
  ;; [{:id 1, :attribute :name, :value Ivanov, :transac-id 536870961, :added true}]]
  (println "%%%%%%%%% elements: " elements)
  (let [columns (reduce into (map #(into #{} (keys (first %))) elements))]
    ;;(println "***** Columns: " columns)
    (div
      (ui-query-input query-input)
      (mtable
        {:title    "Datoms"
         ;; TODO: BUGS: keywords lose their namespace component
         :columns  (mapv (fn [c] {:title c :field c}) columns)
         ;; TODO: BUGS: keywords lose their namespace component
         :data     (if (empty? (first elements))
                     []
                     (mapv non-number-vals-to-str (mapv first elements)))

         :editable {:onRowAdd    (fn [newData]
                                   (do
                                     (comp/transact! this
                                       [(dm/update-datoms {:datoms/datom (into [:db/add]
                                                                           (vec (vals (js->clj newData))))
                                                           :datoms/target-comp :app.dashboard.ui.queries.datoms/Datoms})])
                                     (js/Promise.resolve newData)))

                    :onRowUpdate (fn [newData, oldData]
                                   (do
                                     (comp/transact! this
                                       [(dm/update-datoms {:datoms/datom       (vals (js->clj newData))
                                                           :datoms/target-comp :app.dashboard.ui.queries.datoms/Datoms})])
                                     (js/Promise.resolve newData)))
                    :onRowDelete id}}))))

(def ui-datoms (comp/factory Datoms))




(comment
  (def entity [{:db/id 2 :age 25 :name "Ivan"}])
  (def entities #{[{:db/id 2 :age 25 :name "Ivan"}] [{:db/id 1 :age 44 :name "Petr"}] [{:db/id 3 :player/age 44 :player/event "Petr"}]})

  (def columns (reduce into (map #(into #{} (keys (first %))) entities)))

  columns
  (mapv (fn [c] {:title c :field c}) columns)

  (into #{1 2} (set (keys (first entity))))

  (def res {:db/id 10, :player/event [{:db/id 7} {:db/id 8} {:db/id 11}], :player/name "Paul", :player/team [{:db/id 11} {:db/id 12}]})

  (defn map-vals
    "Maps a function over the values of an associative collection."
    [f m]
    (into {} (map (juxt key (comp f val))) m))

  (defn vec-to-id
    [val]
    (if (vector? val) (str val) val))

  (map-vals vec-to-id res)
  )
