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

(defmutation submit-query-input
  "Submits the query entered manually by user"
  [{:query-input/keys [entity-id selector]}]
  (action [{:keys [state]}]
    ;;(log/info "Replacing datoms with"  value) ;; Prints the clj object in a weird way sometimes
    ;;(println (vals value))
    ;;(p/pprint #_@state)
    (println "In 'submit-query-input: Entity-id is int: " (type entity-id) "// selector is vector?: " (vector? selector))
    #_(swap! state
      (fn [s]
        (-> s
          (merge/merge-ident [:datoms/id :the-datoms]
            {:datoms/id       :the-datoms
             :datoms/elements (into [[0 :name "joe"]]
                                (get-in @state [:datoms/id :the-datoms
                                                :datoms/elements]))}))))
    )

  (ok-action [env]
    (log/info "OK action"))
  (error-action [env]
    (log/info "Error action"))
  (rest-remote [env]
    (eql/query->ast1 `[(pull-query  ~{:query-input/entity-id entity-id
                                      :query-input/selector selector}
                         )])))


(pc/defmutation pull-query [env {:query-input/keys [entity-id selector]}]
  {;;::pc/sym    `pull-query ;; If using 'sym then !!! the quote is a BACK quote
   ::pc/params [:query-input/entity-id :query-input/selector] 
   ::pc/output [:datoms/id]}
  (log/info (str "In pathom-mutations - pull4: -------------- " entity-id " --- " selector ))
                (go (let [d (<! (http/post "http://localhost:3000/pull"
                                             {:with-credentials? false
                                              :headers           {"Content-Type" "application/edn"
                                                                  "Accept"       "application/edn"}
                                              ;;:edn-params        {:eid 1 :selector [:name]} ;; !!!! Does not work if selector is a string (as we are using EDN selector can and should be a vector).
                                              :edn-params        {:eid entity-id :selector selector}
                                              }))]
                      (println "resp???????????: " (type (:body d)))
                      (println "resp???????????: " (:body d))
                        ;;(df/load! SPA :the-datoms dui/Datoms {:remote :rest-remote})
                        {:datoms/id -1})))








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
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                    {:with-credentials? false
                     :headers           {"Content-Type" "application/edn"
                                         "Accept"       "application/edn"}
                     :edn-params        {:tx-data [my-datom]
                                         ;;[[:db/add -1 :player/name "IIIIvanooooo"]]
                                         :tx-meta []}
                              }))]
        (println "resp: " (:body d))
        (println "my good datom: " my-datom)
        ;;(df/load! SPA :the-datoms dui/Datoms {:remote :rest-remote})
        {:datoms/id -1})))



(def mutations [transact-datoms pull-query])



(comment
  (go (let [d (<! (http/post "http://localhost:3000/transact"
                             {:with-credentials? false
                              :headers           {"Content-Type" "application/edn"
                                                  "Accept"       "application/edn"}
                              :edn-params        {:tx-data [[:db/add -1 :player/name "IIIIvano"]]
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

  )
