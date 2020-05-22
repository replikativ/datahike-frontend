(ns app.dashboard.ui.queries.query-input
  (:require
   ;;   [app.dashboard.mutations.query-input :as dm]
   [com.fulcrologic.fulcro.mutations :as m]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [app.dashboard.mutations.datoms :as dm]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody textarea form]]
   ))




(defsc QueryInput [this {:query-input/keys [id content] :as props}]
  {:query [:query-input/id :query-input/content]
   :initial-state (fn [_] {:query-input/id      ":query-input-init-state"
                           :query-input/content "[:find (pull ?e [:*]) :where [?e :url ?b]]"})
   :ident         (fn [] [:query-input/id :the-query-input])
   }
  (div "Enter Query:"
    (form
      ;; '[:find (pull ?e [:*]) :where [?e :url ?b]]
      (textarea {:value content
                 :onChange #(m/set-string! this :query-input/content :event %)})
      (button {:onClick #(comp/transact! this `[(dm/submit-query-input ~{:queries/query content})])}))))

(def ui-query-input (comp/factory QueryInput))
