(ns app.dashboard.ui.queries.schema
  (:require
   [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
   [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b table thead tr th td tbody]]))



;; TODO: BUG: when sreen width is too small, it is no longer possible to click on the 'schema' and 'Datoms' link.
(defsc Schema [this {:schema/keys [id elements] :as props}]
  {:query [:schema/id :schema/elements]
   :initial-state (fn [_] {:schema/id      ":init-state"
                           :schema/elements {}})
   :ident         (fn [] [:schema/id :the-schema])
   :route-segment ["schema"]}
  (table :.ui.cell.table
    (thead
      (tr
        (th "ident")
        (th "valueType")
        (th "cardinality")
        (th "doc")
        (th "index")
        (th "unique")
        (th "noHistory")
        (th "isComponent")))
    (tbody
      ;; TODO: 'when' because the input from db contains some entries without a :db/ident. Fix this later.
      (map #(when (:db/ident %)
              (tr
                (td (str (:db/ident %)))
                (td (str (:db/valueType %)))
                (td (str (:db/cardinality %)))
                (td (str (:db/doc %)))
                (td (str (:db/index %)))
                (td (str (:db/unique %)))
                (td (str (:db/noHistory %)))
                (td (str (:db/isComponent %)))))
        elements))))

(def ui-schema (comp/factory Schema))
