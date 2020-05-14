(ns app.client-resolvers
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    ))



(defresolver datoms-resolver [env input]
  {::pc/output [{:the-datoms [:datoms/id :datoms/elements]}]}
  {:the-datoms {:datoms/id       :the-datoms
                :datoms/elements [[1 :attr 2 3 true]]

                }})


(def resolvers [datoms-resolver])
