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
       (go
         (try
           ;;(log/info "Rest-remote (defn remote...) 2 !!!!!:" edn " ---")
           (result-handler {:transaction edn
                            :body (<?maybe (parser {} edn))
                            :status-code 200})
           (catch :default e
             (js/console.error "Pathom remote error:" e)
             (result-handler {:transaction edn
                              :body e
                              :status-code 500}))))))})

