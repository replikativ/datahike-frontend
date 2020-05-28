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
   [cljs.reader :as reader]
   [cljs.core.async :refer [<!]]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defmutation submit-query-input
  "Submits the query entered manually by user"
  [{:query-input/keys [entity-id selector target-comp]}]
  (action [{:keys [state]}]
    (p/pprint "hello" #_@state))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env]
    (println "in rest-remote" (type selector) )
    (-> env
      (m/with-server-side-mutation `a-pull-query)
      (m/with-params {:query-input/entity-id entity-id
                      :query-input/selector selector})
      (m/returning target-comp))))


(pc/defmutation a-pull-query [env {:query-input/keys [entity-id selector]}]
  {;;::pc/sym    `pull-query ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:query-input/entity-id :query-input/selector] 
   ::pc/output [:datoms/id :datoms/elements]}
  (log/info (str "In pathom-mutations: -------------- " entity-id " --- " selector ))
  (go (let [r        (:body (<! (http/post "http://localhost:3000/q"
                                  {:with-credentials? false
                                   :headers           {"Content-Type" "application/edn"
                                                       "Accept"       "application/edn"}
                                   ;; TODO TODO TODO: !!!!!! Possible Injection attack here or not?
                                   :edn-params        {:query `[:find ~(reader/read-string entity-id)
                                                                :where ~(reader/read-string  selector)]}})) ) ;; [?e :name "IVan"]
            to_datoms (fn [[entity]]
                        (let [eid (:db/id entity)]
                          (vec (map (fn [[attr val]]
                                      [eid attr val])
                                 entity))))]
        (println "response: " r)
        {:datoms/id       :the-datoms
         :datoms/elements (cond
                            (vector? r) (to_datoms r)
                            (set? r) (reduce into (map to_datoms r)))}))) 








(defmutation update-datoms
  "Client Mutation: Replaces the vector which contains all the datoms"
  [{:datoms/keys [datom]}]
  (action [{:keys [state]}]
    ;;(log/info "Replacing datoms with"  value) ;; Prints the clj object in a weird way sometimes
    ;;(println (vals value))
    ;;(p/pprint #_@state)
    (println "datom: " datom)

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
  (rest-remote [env]
    ;;(println "in Fulcro mutation: datom: " (type datom))
    (eql/query->ast1 `[(transact-datoms {:datoms/my-datom ~datom})])))



;; TODO: issues is that my-datom content is made of Strings only
(pc/defmutation transact-datoms [env {:keys [datoms/my-datom]}]
  {::pc/sym    `transact-datoms ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:datoms/my-datom]
   ::pc/output [:datoms/id]}
  (log/info (str "In client-mutations - transact-datoms: --- " (coll? my-datom) "---" ))
  (go (let [d (<! (http/post "http://localhost:3000/q"
                    {:with-credentials? false
                     :headers           {"Content-Type" "application/edn"
                                         "Accept"       "application/edn"}
                     :edn-params        {:tx-data [my-datom]
                                         :tx-meta []}                                       }))]
        (println "resp: " (:body d))
        (println "my good datom: " my-datom)
        ;;(df/load! SPA :the-datoms dui/Datoms {:remote :rest-remote})
        {:datoms/id -1})))



(def mutations [transact-datoms a-pull-query])







(comment
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                             {:with-credentials? false
                              :headers           {"Content-Type" "application/edn"
                                                  "Accept"       "application/edn"}
                              :edn-params        {:tx-data [[:db/add 1 :player/event "Genf"]]
                                                  :tx-meta []}
                              }))]
        (println d)))


  (go (let [d (<! (http/post "http://localhost:3000/pull"
                    {:with-credentials? false
                     :headers           {"Content-Type" "application/edn"
                                         "Accept"       "application/edn"}
                     :edn-params        {:eid 1
                                         ;;[[:db/add -1 :player/name "IIIIvanooooo"]]
                                         :selector [:name]}
                              }))]
        (println "resp: " (:body d))
        ;;(df/load! SPA :the-datoms dui/Datoms {:remote :rest-remote})
        {:datoms/id -1}))


  (def entity [{:db/id 2 :age 25 :name "Ivan"}])
  (def entities #{[{:db/id 2 :age 25 :name "Ivan"}] [{:db/id 1 :age 44 :name "Petr"}]})


  (defn to_datoms [[entity]]
    (let [eid (:db/id entity)]
      (vec (map (fn [[attr val]] [eid attr val]) entity))))

  (to_datoms entity)

  (reduce into (map to_datoms entities))

  )
