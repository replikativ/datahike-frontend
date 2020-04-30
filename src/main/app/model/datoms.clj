(ns app.model.datoms
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [app.datahike.database :refer [conn]]
    [datahike.api :as d]))



(defresolver datoms-resolver [env input]
  {::pc/output [{:the-datoms [:datoms/id :datoms/elements]}]}
  {:the-datoms {:datoms/id       :the-datoms
                :datoms/elements (map (comp vec seq) (d/datoms @conn :eavt))

                }})


(def resolvers [datoms-resolver])



(comment

  )
