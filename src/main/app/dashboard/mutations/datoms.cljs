(ns app.dashboard.mutations.datoms
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [taoensso.timbre :as log]
   [cljs.pprint :as p]
   [app.dashboard.helper :as h]
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
  [{:query-input/keys [pull-expr where-expr target-comp]}]
  (action [{:keys [state]}]
    (p/pprint "hello" #_@state))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env]
    (println "in rest-remote" (type where-expr) )
    (-> env
      (m/with-server-side-mutation `a-pull-query)
      (m/with-params {:query-input/pull-expr pull-expr
                      :query-input/where-expr where-expr})
      (m/returning target-comp))))


;; TODO: rename to 'query
;; TODO: Can't the body of this mutation be moved into a resolver?
(pc/defmutation a-pull-query [env {:query-input/keys [pull-expr where-expr]}]
  {;;::pc/sym    `pull-query ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:query-input/pull-expr :query-input/where-expr]
   ::pc/output [:datoms/id :datoms/elements :datoms/query-input]}
  (log/info (str "In pathom-mutations: -------------- " pull-expr " --- " where-expr ))
  (go (let [r        (:body (<! (http/post "http://localhost:3000/q"
                                  {:with-credentials? false
                                   :headers           {"Content-Type" "application/edn"
                                                       "Accept"       "application/edn"}
                                   ;; TODO TODO TODO: !!!!!! Possible Injection attack here or not?
                                   :edn-params        {:query `[:find ~(reader/read-string pull-expr)
                                                                :where ~(reader/read-string  where-expr)]}})))]
        (println "response: " r)
        {:datoms/id       :the-datoms
         :datoms/elements (cond
                            (vector? r)  #{r}
                            (set? r)     r)
         :datoms/query-input {:query-input/id :the-query-input
                              :query-input/pull-expr pull-expr
                              :query-input/where-expr where-expr}})))





(defmutation update-datoms
  "Client Mutation: updates a datom"
  [{:datoms/keys [datom target-comp]}]
  (action [{:keys [state]}]
    ;;(p/pprint #_@state)
    (log/info "In update-datoms's action"))
  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    ;; TODO Show error when happening
    (log/info "Error action"))
  (rest-remote [env]
    ;;(println "in Fulcro mutation: datom: " datom #_(type datom))
    (-> env
      (m/with-server-side-mutation `transact-datoms)
      (m/with-params {:datoms/my-datom datom})
      (m/returning target-comp))
    ;;(eql/query->ast1 `[(transact-datoms {:datoms/my-datom ~datom})])
    ))


;; TODO: rename my-datom to entity?
(pc/defmutation transact-datoms [env {:keys [datoms/my-datom]}]
  {::pc/params [:datoms/my-datom]
   ::pc/output [:datoms/id :datoms/elements :datoms/query-input]}
  (go (let [tx-data [(h/str-to-clj my-datom)
                     #_[:db/add (first my-datom)
                      ;; TODO: The below line converts the string ":event/name" into a keyword.
                      ;; Isn't the read-string subject to injection attack?
                      ;; Using (keyword ":event/name") does not work as it gives ::event/name.
                      (reader/read-string (nth my-datom 1))
                        (nth my-datom 2)]]
            ;;_ (println "+++++++ tx-data:" tx-data)
            ;;_ (println "In client-mutations - transact-datoms: --- " (h/str-to-clj my-datom))
            d (<! (http/post "http://localhost:3000/transact"
                    {:with-credentials? false
                     :headers           {"Content-Type" "application/edn"
                                         "Accept"       "application/edn"}
                     ;; TODO: Below works only when the table is in :eavt form
                     ;; i.e., will not work when the table shows an entity in one row
                     :edn-params        {:tx-data tx-data
                                         :tx-meta []}}))]
        ;; (println "resp: " (:body d))
        {:datoms/id :the-datoms})))



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
                                         :where-expr [:name]}
                              }))]
        (println "resp: " (:body d))
        {:datoms/id -1}))


  (def entity [{:db/id 2 :age 25 :name "Ivan"}])
  (def entities #{[{:db/id 2 :age 25 :name "Ivan"}] [{:db/id 1 :age 44 :name "Petr"}]})


  (defn to_datoms [[entity]]
    (let [eid (:db/id entity)]
      (vec (map (fn [[attr val]] [eid attr val]) entity))))

  (to_datoms entity)

  (reduce into (map to_datoms entities))

  )
