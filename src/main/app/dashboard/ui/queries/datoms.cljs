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

;; For /q
;;
;;TODO: Rename entity-id
;;TODO: rename 'selector
(defsc QueryInput [this {:query-input/keys [id entity-id selector] :as props}]
  {:query [:query-input/id :query-input/entity-id :query-input/selector]
   :initial-state (fn [_] {:query-input/id      ":query-input-init-state"
                           :query-input/selector "[?e :name 'IVan']"
                           :query-input/entity-id  "[(pull ?e [*])]"})
   :ident         (fn [] [:query-input/id :the-query-input])}
  (div
    (div
      (label "Pull expr:"
        ;; TODO: try to use this same string set in the resolver
        (textarea {:value    "[(pull ?e [*])]"
                   #_:onChange #_(fn [event]
                                   (println "---->" (type event))
                                   (m/set-string! this :query-input/entity-id :event event))})))
    (div
      (label "Where:"
        ;; TODO BUG:  a simple paste into the field is not considered a change to the field so the fields is still seen as empty => crash
        (textarea {:value selector
                   :onChange #(m/set-string! this :query-input/selector :event %)})))

    (div
      (println "entity-id "  entity-id  "type: " (type entity-id))
      (println "Selector: " selector)
      (button {:onClick
               (fn []

                 (println "after submit: entity-id " (type entity-id))
                 (println "after submit: Selector: " selector)
                 (comp/transact! this [(dm/submit-query-input
                                         {:query-input/target-comp :app.dashboard.ui.queries.datoms/Datoms
                                          ;; TODO restore 'selector
                                          :query-input/selector "[?e :name \"IVan\"]" ;; selector
                                          ;; TODO: use the var entity-id here once we use this same string set in the resolver
                                          :query-input/entity-id  "[(pull ?e [*])]"})
                                       (comp/get-query Datoms)]))}
        "Query!"))))

(def ui-query-input (comp/factory QueryInput))




(def mtable (interop/react-factory MaterialTable))


(defsc Datoms [this {:datoms/keys [id elements] :as props}]
  {:query [:datoms/id :datoms/elements]
   :initial-state (fn [_] {:datoms/id      ":datoms-init-state"
                           :datoms/elements {}})
   :ident         (fn [] [:datoms/id :the-datoms])
   :route-segment ["datoms"]}
  ;; Expects: [[{:id 1, :attribute :age, :value 31, :transac-id 536870961, :added true}]
  ;; [{:id 1, :attribute :name, :value Ivanov, :transac-id 536870961, :added true}]]
  (println "%%%%%%%%% elements: " elements)
  (let [columns (reduce into (map #(into #{} (keys (first %))) elements))]
    ;;(println "***** Columns: " columns)
    (div
      (ui-query-input)
      (mtable
        {:title    "Datoms"
         :columns  (mapv (fn [c] {:title c :field c}) columns)

         :data     (if (empty? (first elements))
                     []
                     (mapv first elements))

         :editable {:onRowAdd    (fn [newData]
                                   (do
                                     (comp/transact! this
                                       [(dm/update-datoms {:datoms/datom (into [:db/add]
                                                                           (vec (vals (js->clj newData))))})])
                                     (js/Promise.resolve newData)
                                     ))

                    :onRowUpdate (fn [newData, oldData]
                                   (do
                                     ;; do the defmutation here
                                     (comp/transact! this [(dm/update-datoms {:datoms/datom (vals (js->clj newData))})])
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

  )
