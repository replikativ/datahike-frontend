(ns app.dashboard.resolvers.schema
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(defresolver schema-resolver [env input]
  {::pc/output [{:the-schema [:schema/id :schema/elements]}]}
  (go
    (let [r (<! (http/get "http://localhost:3000/schema"
                  {:with-credentials? false
                   :headers {"Content-Type" "application/transit+json"
                             "Accept"       "application/transit+json"}}))]
      (println r)
      {:the-schema {:schema/id       :the-schema
                    :schema/elements (vec (vals (filter (comp keyword? key)
                                                  (:body r))))}})))



(def resolvers [schema-resolver])







(comment
  (fetch-schema)
  (all-datoms)

  (go (let [d (<! (http/get "http://localhost:3000/schema"
                    {:with-credentials? false
                     :headers {"Content-Type" "application/transit+json"
                               "Accept"       "application/transit+json"}}
                             ))]
        (println d)))

  )
