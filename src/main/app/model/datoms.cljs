(ns app.model.datoms
  (:require
   [taoensso.timbre :as log]
   [cljs.pprint :as p]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))


(defmutation update-datoms
  "Client Mutation: Replaces the vector which contains all the datoms"
  [{:datoms/keys [datom]}]
  (action [{:keys [state]}]
    ;;(log/info "Replacing datoms with"  value) ;; Prints the clj object in a weird way sometimes
    ;;(println (vals value))
    ;;(p/pprint #_@state)
    
    (swap! state (fn [s]
                   (-> s 
                     (merge/merge-ident [:datoms/id :the-datoms]
                       {:datoms/id       :the-datoms
                        :datoms/elements (into [(vec datom)] (get-in @state [:datoms/id
                                                                             :the-datoms
                                                                               :datoms/elements]))})
                     ))))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  #_(remote [env]
    (-> env
      (m/returning 'app.ui.root/User)
      (m/with-target (targeting/append-to [:all-accounts])))))
