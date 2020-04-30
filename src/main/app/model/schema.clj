(ns app.model.schema
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [app.datahike.database :refer [conn]]
    [datahike.db :as dd]))



(defresolver schema-resolver [env input]
  {::pc/output [{:the-schema [:schema/id :schema/elements]}]}
  {:the-schema {:schema/id       :the-schema
                :schema/elements (vec (vals (filter (comp keyword? key)
                                              (dd/-schema @conn))))}})


(def resolvers [schema-resolver])



(comment

  )
