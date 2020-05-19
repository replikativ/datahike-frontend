(ns app.dashboard.mutations.datoms
  (:require
   [taoensso.timbre :as log]
   [cljs.pprint :as p]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.wsscode.pathom.connect :as pc]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [edn-query-language.core :as eql]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))


(defmutation update-datoms
  "Client Mutation: Replaces the vector which contains all the datoms"
  [{:datoms/keys [datom]}]
  (action [{:keys [state]}]
    ;;(log/info "Replacing datoms with"  value) ;; Prints the clj object in a weird way sometimes
    ;;(println (vals value))
    ;;(p/pprint #_@state)
    
    (swap! state
      (fn [s]
        (-> s
          (merge/merge-ident [:datoms/id :the-datoms]
            {:datoms/id       :the-datoms
             :datoms/elements (into [(vec datom)]
                                (get-in @state [:datoms/id
                                                :the-datoms
                                                :datoms/elements]))})))))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env] (eql/query->ast1 `[(send-message {:message/text "hello"})]))

  #_(rest-remote [env]
    (log/info "In (datoms.cljs rest-remote)!!!!!")
    true))


(pc/defmutation send-message [env {:keys [message/text]}]
  {;;::pc/sym    `send-message ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:message/text]
   ::pc/output [:message/id :message/text]}
  (log/info "In client-mutations - send-message !!!!!")
  {:message/id   123
   :message/text text})



(def mutations [send-message])
