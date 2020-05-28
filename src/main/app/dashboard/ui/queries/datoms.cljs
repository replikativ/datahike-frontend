(ns app.dashboard.ui.queries.datoms
  (:require
   [app.dashboard.mutations.datoms :as dm]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody label textarea]]
   [com.fulcrologic.fulcro.mutations :as m]
   ["material-table" :default MaterialTable]))


(declare Datoms)

;; For /q
(defsc QueryInput [this {:query-input/keys [id entity-id selector] :as props}]
  {:query [:query-input/id :query-input/entity-id :query-input/selector]
   :initial-state (fn [_] {:query-input/id      ":query-input-init-state"
                           :query-input/selector "[?e :name 'IVan']"
                           :query-input/entity-id  "[(pull ?e [*])]"})
   :ident         (fn [] [:query-input/id :the-query-input])}
  (div
    (div
      (label "Pull expr:"
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
                                          :query-input/selector selector
                                          :query-input/entity-id  "[(pull ?e [*])]"})
                                       (comp/get-query Datoms)]))}
        "Query!"))))


;; When used for pull-query (i.e. with eid and selector to be entered by user
#_(defsc QueryInput [this {:query-input/keys [id entity-id selector] :as props}]
  {:query [:query-input/id :query-input/entity-id :query-input/selector]
   :initial-state (fn [_] {:query-input/id      ":query-input-init-state"
                           :query-input/selector "[:name]"
                           :query-input/entity-id  "1"})
   :ident         (fn [] [:query-input/id :the-query-input])}
  (div "Pull Query:"
    (div
      (label "Entity id:"
        (textarea {:value    (str entity-id)
                   :onChange (fn [event]
                               (println "---->" (type event))
                               (m/set-string! this :query-input/entity-id :event event))})))
    (div
      (label "Selector:"
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
                                         ;; TODO TODO TODO: !!!!!! STOP using read-string as it is a huge security risk!
                                         {:query-input/target-comp :app.dashboard.ui.queries.datoms/Datoms
                                          :query-input/selector (reader/read-string selector)
                                          ;; WEIRD: sometimes noticing that entity-id is no longer a Number but becomes a String when system reaches this point.
                                          :query-input/entity-id (if (int? entity-id)
                                                                   entity-id
                                                                   (reader/read-string entity-id))
                                          })
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
  (div
    (ui-query-input)
    (mtable
      {:title    "Datoms"
       :columns  [
                  {:title "Entity" :field :entity}
                  {:title "Attributes" :field :attributes}
                  {:title "Value" :field :value}
                  {:title "Transac. id" :field :tr_id}
                  {:title "Added" :field :added}]

       :data     (map (fn [datom] {:entity     (first datom)
                                   :attributes (str (nth datom 1))
                                   :value      (nth datom 2)
                                   :tr_id      (if (> (count datom) 3) (nth datom 3) "")
                                   :added      (if (> (count datom) 4) (nth datom 4) "")
                                   })
                   elements)

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
                  :onRowDelete id}})))

(def ui-datoms (comp/factory Datoms))
