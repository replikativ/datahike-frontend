(ns app.dashboard.resolvers.datoms
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [cljs-http.client :as http]
    [cljs.core.async :refer [<!]]))

(defresolver datoms-resolver [env input]
  {::pc/output [{:the-datoms [:datoms/id :datoms/elements]}]}
  (go
    (let [r (<! (http/post "http://localhost:3000/datoms"
                  {:with-credentials? false
                   :headers {"Content-Type" "application/transit+json"
                             "Accept"       "application/transit+json"}
                   :transit-params {:index :eavt}}))]
      ;;(println r)
      {:the-datoms {:datoms/id       :the-datoms
                    :datoms/elements (:body r)}})))

(def resolvers [datoms-resolver])







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
      (println r)
      ))

  )
