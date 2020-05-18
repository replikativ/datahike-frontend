(ns app.client-resolvers
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.core.async :refer [go]]
    [ajax.core :refer [GET PUT DELETE POST]]
    ))


(defn fetch-schema []
  (GET "http://localhost:3000/schema"
       {:handler (fn [r] (println "GET schema returned: " r) ;;(swap! state update-in [:schema] #(merge r core-schema))
                   )
        :headers {"Content-Type" "application/transit+json"
                  "Accept" "application/transit+json"}}))

;; Why is this a POST!?
#_(defn all-datoms [index]
  (POST "http://localhost:3000/datoms"
        {:handler (fn [r]
                    (swap! state assoc-in [:last-datoms] r))
         :params {:index :eavt}
         :headers {"Content-Type" "application/transit+json"
                   "Accept" "application/transit+json"}}))




(defresolver datoms-resolver [env input]
  {::pc/output [{:the-datoms [:datoms/id :datoms/elements]}]}

  ;; {:the-datoms {:datoms/id       :the-datoms
  ;;               :datoms/elements [[1 :attr 2 3 true]]}}

  (let [name    (-> env :ast :params :name)
        params  {"name"      name}]
    (go
      (let [ _ (fetch-schema)
            ;;{:keys [body]} (<! (http/get "https://myrest.com/search" {:query-params      params}))
            ]
        {:the-datoms {:datoms/id       :the-datoms
                      :datoms/elements [[1 :attr 2 3 true]]}}
        ))))


(def resolvers [datoms-resolver])



(comment
  (fetch-schema)
  )
