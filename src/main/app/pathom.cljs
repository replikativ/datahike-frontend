(ns app.pathom
  (:require
   [com.wsscode.common.async-cljs :refer [go-catch <?]]
   [com.wsscode.pathom.core :as p]
   [com.wsscode.pathom.connect :as pc]
   [com.wsscode.pathom.connect.graphql2 :as pcg]
   [com.wsscode.pathom.diplomat.http :as p.http]
   [com.wsscode.pathom.diplomat.http.fetch :as p.http.fetch]
   [app.client-mutations :as client-mutations]))


(def all-resolvers [;;schema/resolvers datoms/resolvers acct/resolvers session/resolvers
                    ;;index-explorer
                    client-mutations/mutations])

(def parser
  (p/parallel-parser
    {::p/mutate pc/mutate-async
     ::p/env {::p/reader [p/map-reader
                          pc/parallel-reader
                          pc/open-ident-reader
                          p/env-placeholder-reader]
              ::p/placeholder-prefixes #{">"}
              ::p.http/driver p.http.fetch/request-async}
     ::p/plugins [(pc/connect-plugin {::pc/register all-resolvers})
                  p/error-handler-plugin
                  p/request-cache-plugin
                  p/trace-plugin]}))
