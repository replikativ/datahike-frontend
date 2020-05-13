(ns app.rest-remote
  (:require [com.wsscode.pathom.connect :as pc]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.fulcro.network :as pn]
            [clojure.core.async :as async]
            [com.fulcrologic.fulcro.components :as comp]
            ))


(defmulti resolver-fn pc/resolver-dispatch)
(defonce indexes (atom {}))
(defonce defresolver (pc/resolver-factory resolver-fn indexes))

(defn rest-parser
  "Create a REST parser. Make sure you've required all nses that define rest resolvers. The given app-atom will be available
  to all resolvers in `env` as `:app-atom`."
  [extra-env]
  (p/async-parser
    {::p/plugins [(p/env-plugin
                    (merge extra-env
                      {::p/reader             [p/map-reader
                                               pc/all-async-readers]
                       :app-atom              app-atom
                       ::pc/resolver-dispatch resolver-fn
                       ::pc/indexes           @indexes}))
                  p/request-cache-plugin
                  (p/post-process-parser-plugin p/elide-not-found)]}))

(defn rest-remote [extra-env]
  (pn/pathom-remote (rest-parser extra-env)))
