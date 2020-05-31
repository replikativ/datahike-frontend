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


;;TODO: rename to 'query
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
                                                                :where ~(reader/read-string  selector)]}}))) ;; [?e :name "IVan"]
            to_datoms (fn [[entity]]
                        (let [eid (:db/id entity)]
                          (vec (map (fn [[attr val]]
                                      [eid attr val])
                                 entity))))]
        (println "response: " r)
        {:datoms/id       :the-datoms
         :datoms/elements (cond
                            (vector? r)  #{r}
                            (set? r)     r)})))





(defmutation update-datoms
  "Client Mutation: updates a datom"
  [{:datoms/keys [datom]}]
  (action [{:keys [state]}]
    ;;(p/pprint #_@state)
    (log/info "In update-datoms's action"))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env]
    ;;(println "in Fulcro mutation: datom: " (type datom))
    (eql/query->ast1 `[(transact-datoms {:datoms/my-datom ~datom})])))



(pc/defmutation transact-datoms [env {:keys [datoms/my-datom]}]
  {::pc/params [:datoms/my-datom]
   ::pc/output [:datoms/id]}
  (log/info (str "In client-mutations - transact-datoms: --- " my-datom (coll? my-datom) "---" ))
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                             {:with-credentials? false
                              :headers           {"Content-Type" "application/edn"
                                                  "Accept"       "application/edn"}
                              ;; TODO: Below works only when the table is in :eavt form
                              ;; i.e., will not work when the table shows an entity in one row
                              :edn-params        {:tx-data [[:db/add (first my-datom)
                                                             (keyword (nth my-datom 1))
                                                             (nth my-datom 2)]]
                                                  :tx-meta []}}))]
        ;; (println "resp: " (:body d))
        ;; (println "my good datom: " my-datom)
        ;; TODO: Find a way to reload the table from here
        {:datoms/id :the-datoms}
        )))



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
