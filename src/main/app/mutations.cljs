(ns app.mutations
  (:require [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
            [com.fulcrologic.fulcro.algorithms.merge :as merge]
            [com.fulcrologic.fulcro.algorithms.form-state :as fs]))

(defmutation delete-dish
  "Mutation: Delete the dish with `id`"
  ;; --- This was in the book but it does not work.
  ;; [{day-menu-id :day-menu/id
  ;;   dish-id :dish/id}]
  [{:keys [day-menu-id dish-id]}] ;; the arguments for the mutation itself
  (action [{:keys [state]}] ; (2)
    (println " ---- In delete-dish:" day-menu-id dish-id @state )
    ;;(cljs.pprint/pprint "state before: " @state)
    (swap! state merge/remove-ident* [:dish/id dish-id] [:day-menu/id day-menu-id :day-menu/dish-day-menu]))
  (remote [env] true))


(defmutation submit-week-desc
  "Mutation: Change the `week-desc` value"
  [{:keys [week-desc]}] ;; the arguments for the mutation itself
  (action [{:keys [state]}] ; (2)
    ;; Updates form state to think the form is now in pristine shape.
    ;; [:menu/id :menu] is our singleton menu
    (swap! state fs/entity->pristine* [:menu/id :menu]))
  (remote [env] true))
