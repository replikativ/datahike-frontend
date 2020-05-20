(ns app.dashboard.ui.queries.datoms
  (:require
   [app.dashboard.mutations.datoms :as dm]
   [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody]]
   ["material-table" :default MaterialTable]))



(def mtable (interop/react-factory MaterialTable))


(defsc Datoms [this {:datoms/keys [id elements] :as props}]
  {:query [:datoms/id :datoms/elements]
   :initial-state (fn [_] {:datoms/id      ":datoms-init-state"
                           :datoms/elements {}})
   :ident         (fn [] [:datoms/id :the-datoms])
   :route-segment ["datoms"]}
  (div
    (mtable
      {:title    "Datoms"
       :columns  [
                  {:title "Entity"       :field :entity}
                  {:title "Attributes"   :field :attributes}
                  {:title "Value"        :field :value}
                  {:title "Transac. id"  :field :tr_id}
                  {:title "Added"        :field :added}]

       :data     (map (fn [datom] {:entity     (first datom)
                                   :attributes (str (nth datom 1))
                                   :value      (nth datom 2)
                                   :tr_id      (nth datom 3)
                                   :added      (nth datom 4)})
                      elements)

       :editable {:onRowAdd    id
                  :onRowUpdate (fn [newData, oldData]
                                 (do
                                   ;; do the defmutation here
                                   (comp/transact! this [(dm/update-datoms {:datoms/datom (vals (js->clj newData))})])
                                   (js/Promise.resolve newData)))
                  :onRowDelete id}})))

(def ui-datoms (comp/factory Datoms))
