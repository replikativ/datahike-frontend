(ns app.rest-remote 
  (:require
   [clojure.core.async :refer [go]]
   [com.wsscode.async.async-cljs :refer [<?maybe]]
   [com.fulcrologic.fulcro.algorithms.tx-processing :as txn]
   [edn-query-language.core :as eql]
   [taoensso.timbre :as log] 
   ))

(defn remote [parser]
  {:transmit!
   (fn [_ {::txn/keys [ast result-handler]}]
     (let [edn (eql/ast->query ast)]
       (log/info "Rest-remote (defn remote...) 1 !!!!!")
       (go
         (try
           (log/info "Rest-remote (defn remote...) 2 !!!!!:" edn " ---")
           (result-handler {:transaction edn
                            :body (<?maybe (parser {} edn))
                            :status-code 200})
           (catch :default e
             (js/console.error "Pathom remote error:" e)
             (result-handler {:transaction edn
                              :body e
                              :status-code 500}))))))})




;; TODO TO_DELETE
;; (ns app.rest-remote
;;   (:require [com.wsscode.pathom.connect :as pc]
;;             [com.wsscode.pathom.core :as p]
;;             [com.wsscode.pathom.fulcro.network :as pn]
;;             [clojure.core.async :as async]
;;             [com.fulcrologic.fulcro.components :as comp]
;;             ))


;; (defmulti resolver-fn pc/resolver-dispatch)
;; (defonce indexes (atom {}))
;; (defonce defresolver (pc/resolver-factory resolver-fn indexes))

;; (defn rest-parser
;;   "Create a REST parser. Make sure you've required all nses that define rest resolvers. The given app-atom will be available
;;   to all resolvers in `env` as `:app-atom`."
;;   [extra-env]
;;   (p/async-parser
;;     {::p/plugins [(p/env-plugin
;;                     (merge extra-env
;;                       {::p/reader             [p/map-reader
;;                                                pc/all-async-readers]
;;                        :app-atom              app-atom
;;                        ::pc/resolver-dispatch resolver-fn
;;                        ::pc/indexes           @indexes}))
;;                   p/request-cache-plugin
;;                   (p/post-process-parser-plugin p/elide-not-found)]}))

;; (defn rest-remote [extra-env]
;;   (pn/pathom-remote (rest-parser extra-env)))
