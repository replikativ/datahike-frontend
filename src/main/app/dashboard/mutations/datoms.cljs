(ns app.dashboard.mutations.datoms
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [taoensso.timbre :as log]
   [cljs.pprint :as p]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.wsscode.pathom.connect :as pc]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [edn-query-language.core :as eql]
   [cljs-http.client :as http]
   [cljs.core.async :refer [<!]]
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
                                (get-in @state [:datoms/id :the-datoms
                                                :datoms/elements]))})))))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env] (eql/query->ast1 `[(transact-datoms {:datoms/datom datom})])))



(pc/defmutation transact-datoms [env {:keys [datoms/datom]}]
  {;;::pc/sym    `transact-datoms ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:datoms/datom]
   ::pc/output [:datoms/id]}
  (log/info "In client-mutations - transact-datoms !!!!!")
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                    {:with-credentials? false
                     :headers {"Content-Type" "application/transit+msgpack"
                               "Accept"       "application/transit+msgpack"}
                     :transit-params {:tx-data [[:db/add -1 :name "Ivan"]],
                                      :tx-meta [{}]}}))]
        (println "resp: " d)
        {:datom/id -1})))



(def mutations [transact-datoms])



(comment
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                    {:with-credentials? false
                     :headers {"Content-Type" "application/transit+msgpack"
                               "Accept"       "application/transit+msgpack"}
                     :transit-params {:tx-data [[:db/add -1 :name "Ivan"]],
                                      :tx-meta [{}]
                                      }}))]
        (println d)))


  )
