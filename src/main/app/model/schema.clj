(ns app.model.schema
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]))
  


(defresolver schema-resolver [env input]
  ;; {::pc/output [{:datahike/schema [:schema/ident
  ;;                                  :schema/value-type]}]}
  {::pc/output [{:the-schema [:schema/id]}]}
  {:the-schema {:schema/id :the-schema
                :schema/elements [{:schema/ident       ":event/name"
                                   :schema/value-type  ":db.type/string"}
                                  {:schema/ident       ":player/name"
                                   :schema/value-type  ":db.type/string"}]}})



(def resolvers [schema-resolver])


;; (def list-table
;;   {:friends {:list/id     :friends
;;              :list/label  "Friends"
;;              :list/people [1 2]}
;;    :enemies {:list/id     :enemies
;;              :list/label  "Enemies"
;;              :list/people [4 3]}})

;; (defresolver all-users-resolver [{:keys [db]} input]
;;   {;;GIVEN nothing (e.g. this is usable as a root query)
;;    ;; I can output all accounts. NOTE: only ID is needed...other resolvers resolve the rest
;;    ::pc/output [{:all-accounts [:account/id]}]}
;;   {:all-accounts (mapv
;;                    (fn [id] {:account/id id})
;;                    (all-account-ids db))})


;; (def menu-table
;;   (atom
;;     {:menu {:menu/id :menu
;;      q       :menu/week-desc "333 avril 2012"
;;             :menu/day-menu-list [1 2]}}))

;; (pc/defresolver the-menu-resolver [env input]
;;   {::pc/output [{:menu [:menu/id]}]}
;;   {:menu {:menu/id :menu}})



;; (def menu-table
;;   (atom
;;     {:menu {:menu/id :menu
;;             :menu/week-desc "333 avril 2012"
;;             :menu/day-menu-list [1 2]}}))

