(ns app.model.schema
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]))
  


(defresolver schema-resolver [env input]
  {::pc/output [{:the-schema [:schema/id :schema/elements]}]}
  {:the-schema {:schema/id       :the-schema
                :schema/elements [{:schema/ident       ":event/name"
                                   :schema/value-type  ":db.type/string"}
                                  {:schema/ident       ":player/name"
                                   :schema/value-type  ":db.type/string"}]}})


(def resolvers [schema-resolver])
