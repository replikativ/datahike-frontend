(ns app.dashboard.resolvers.datoms
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(defresolver all-datoms-resolver [env input]
  {::pc/output [{:the-datoms [:datoms/id :datoms/elements :datoms/query-input :datoms/view-type]}]}
  {:the-datoms {:datoms/id :the-datoms
                :datoms/view-type :eavt }})


(defresolver datoms-resolver [env id]
  {::pc/input #{:datoms/id}
   ::pc/output [:datoms/id :datoms/elements :datoms/query-input :datoms/view-type]}
  (go
    ;; TODO: abstract the call, e.g. use config to set the server name, extract the params, etc...
    (let [datoms (:body (<! (http/post "http://localhost:3000/datoms"
                              {:with-credentials? false
                               :headers {"Content-Type" "application/transit+json"
                                         "Accept"       "application/transit+json"}
                               :transit-params {:index :eavt}})))]
      ;;(println "================ In datoms-resolver: " datoms)
      {:datoms/id       id
       :datoms/elements (mapv (fn [d] [(zipmap [:id :attribute :value :transac-id :added] d)])
                          datoms)
       :datoms/query-input {:query-input/id :the-query-input}
       :datoms/view-type :eavt})))


(defresolver query-input-resolver [env input]
  {::pc/input #{:query-input/id}
   ::pc/output [:query-input/id :query-input/pull-expr :query-input/where-expr]}
  {:query-input/id input
   :query-input/pull-expr "[(pull ?e [*])]"
   :query-input/where-expr "[?e _ _]"})

(def resolvers [all-datoms-resolver datoms-resolver query-input-resolver])







(comment
  (fetch-schema)
  (all-datoms)

  (go (let [d (<! (http/get "http://localhost:3000/schema"
                    {:with-credentials? false
                     :headers {"Content-Type" "application/transit+json"
                               "Accept"       "application/transit+json"}}
                             ))]
        (println d)))


  (go
    (let [r (<! (http/post "http://localhost:3000/datoms"
                  {:with-credentials? false
                   :headers {"Content-Type" "application/transit+json"
                             "Accept"       "application/transit+json"}
                   :transit-params {:index :eavt}
                   }))]
      (println (mapv (fn [d] [(zipmap [:id :attribute :value :transac-id :added] d)])
                           (:body r))
        )
      ))

  )
