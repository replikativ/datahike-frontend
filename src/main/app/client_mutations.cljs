(ns app.client-mutations
  (:require
    [com.wsscode.pathom.connect :as pc]
    [taoensso.timbre :as log]))


#_(pc/defmutation update-datoms [env params]
  (log/info "In client-mutations - update-datoms !!!!!"))


;; (pc/defmutation update-datoms [env {:keys [message/text]}]
;;   {::pc/sym    'update-datoms
;;    ::pc/params [:message/text]
;;    ::pc/output [:message/id :message/text]}
;;   (log/info "In client-mutations - update-datoms !!!!!")
;;   {:message/id   123
;;    :message/text text})

#_(def mutations [update-datoms])
