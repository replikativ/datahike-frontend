(ns app.dashboard.ui.queries.query-input
  (:require
   ;;   [app.dashboard.mutations.query-input :as dm]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [app.dashboard.mutations.datoms :as dm]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody textarea form label]]
   ))




(defsc QueryInput [this {:query-input/keys [id entity-id selector] :as props}]
  {:query [:query-input/id :query-input/entity-id :query-input/selector]
   :initial-state (fn [_] {:query-input/id      ":query-input-init-state"
                           :query-input/selector "[:name]"
                           :query-input/entity-id  "1"})
   :ident         (fn [] [:query-input/id :the-query-input])}
  (div "Pull Query:"
    (div
      (label "Entity id:"
        (textarea {:value (str entity-id)
                   :onChange #(m/set-string! this :query-input/entity-id :event %)})))
    (div
      (label "Selector:"
        (textarea {:value selector
                   :onChange #(m/set-string! this :query-input/selector :event %)})))

    (div
      (println "entity-id " entity-id)
      (button {:onClick #(comp/transact! this `[(dm/submit-query-input {:query-input/selector selector
                                                                        :query-input/entity-id entity-id})])}
        "Submit"))))

(def ui-query-input (comp/factory QueryInput))
