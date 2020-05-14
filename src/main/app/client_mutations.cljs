(ns app.client-mutations
  (:require
    [com.wsscode.pathom.connect :as pc]
    [taoensso.timbre :as log]))



(pc/defmutation update-datoms [env {:datoms/keys [datom]}]
  (log/info "In client-mutations - update-datoms !!!!!")
  )

(def mutations [update-datoms])
