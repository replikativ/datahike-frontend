(ns app.model.menu
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    ))



(def dish-table
  (atom
    {1 {:dish/id 1 :dish/name "Poulet"}
     2 {:dish/id 2 :dish/name "Saucisse"}
     3 {:dish/id 3 :dish/name "Poisson"}
     4 {:dish/id 4 :dish/name "Légumes"}}))

(def day-menu-table
  (atom
    {1 {:day-menu/id     1
        :day-menu/day     "Monday"
        :day-menu/starter  "Mozza"
        :day-menu/dish-list [1 2]}
     2 {:day-menu/id     2
        :day-menu/day     "Tuesday"
        :day-menu/starter  "Crevettes"
        :day-menu/dish-list [4 3]}}))

(def menu-table
  (atom
    {:menu {:menu/id :menu
            :menu/week-desc "333 avril 2012"
            :menu/day-menu-list [1 2]}}))

;; Gdish/id, this can generate the details of a dish
(pc/defresolver dish-resolver [env {:dish/keys [id]}]
  {::pc/input  #{:dish/id}
   ::pc/output [:dish/name]}
   (get @dish-table id))

;; Given a :day-menu/id, this can generate a starter and a dish list
;; (but just with their IDs)
(pc/defresolver day-menu-resolver [env {:day-menu/keys [id]}]
  {::pc/input  #{:day-menu/id}
   ::pc/output [:day-menu/day :day-menu/starter {:day-menu/dish-list [:dish/id]}]}
  (when-let [day-menu (get @day-menu-table id)]
    (assoc day-menu
      :day-menu/starter (:day-menu/starter day-menu)
      :day-menu/dish-list (mapv
                            (fn [id] {:dish/id id})
                            (:day-menu/dish-list day-menu)))))

(pc/defresolver menu-resolver [env {:menu/keys [id]}]
  {::pc/input  #{:menu/id}
   ::pc/output [:menu/week-desc {:menu/day-menu-list [:day-menu/id]}]}
  (when-let [menu (get @menu-table id)]
    (assoc menu
      :menu/day-menu-list (mapv
                            (fn [id] {:day-menu/id id})
                            (:menu/day-menu-list menu)))))


(pc/defresolver the-menu-resolver [env input]
  {::pc/output [{:menu [:menu/id]}]}
  {:menu {:menu/id :menu}})


;; (def dish-table
;;   (atom
;;     {1 {:dish/id 1 :dish/name "Poulet"}
;;      2 {:dish/id 2 :dish/name "Saucisse"}
;;      3 {:dish/id 3 :dish/name "Poisson"}
;;      4 {:dish/id 4 :dish/name "Légumes"}}))

;; (def day-menu-table
;;   (atom
;;     {1 {:day-menu/id     1
;;         :day-menu/day     "Monday"
;;         :day-menu/starter  "Mozza"
;;         :day-menu/dish-list [1 2]}
;;      2 {:day-menu/id     2
;;         :day-menu/day     "Tuesday"
;;         :day-menu/starter  "Crevettes"
;;         :day-menu/dish-list [4 3]}}))

;; (def menu-table
;;   (atom
;;     {:menu {:menu/id :menu
;;             :menu/week-desc "333 avril 2012"
;;             :menu/day-menu-list [1 2]}}))



;; (def menu-table
;;   {1 {:menu/id 1 :menu/week-desc "menu du 3 avril" :menu/menus [1]}})

;; (def day-menu-table
;;   {1 {:day-menu/id 1 :day-menu/starter "Tomates mozza" :day-menu/dish-list [1 2]}})


;; (def dish-table
;;   {1 {:dish/id 1 :dish/name "Poulet"}
;;    2 {:dish/id 2 :dish/name "Frites"}})

;; (defresolver menu-resolver [env {:menu/keys [id]}]
;;   {::pc/input #{:menu/id}
;;    ::pc/output [:menu/week-desc]}
;;   (get menu-table id))


;; (defresolver dish-resolver [env {:dish/keys [id]}]
;;   {::pc/input #{:dish/id}
;;    ::pc/output [:dish/name]}
;;   (get dish-table id))

;; (def menu-resolvers [menu-resolver dish-resolver])


;; =====================


(def people-table
  {1 {:person/id 1 :person/name "Sally" :person/age 32}
   2 {:person/id 2 :person/name "Joe" :person/age 22}
   3 {:person/id 3 :person/name "Fred" :person/age 11}
   4 {:person/id 4 :person/name "Bobby" :person/age 55}})

(def list-table
  {:friends {:list/id     :friends
             :list/label  "Friends"
             :list/people [1 2]}
   :enemies {:list/id     :enemies
             :list/label  "Enemies"
             :list/people [4 3]}})

;; Given :person/id, this can generate the details of a person
(defresolver person-resolver [env {:person/keys [id]}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/name :person/age]}
  (get people-table id))

;; Given a :list/id, this can generate a list label and the people
;; in that list (but just with their IDs)
(defresolver list-resolver [env {:list/keys [id]}]
  {::pc/input  #{:list/id}
   ::pc/output [:list/label {:list/people [:person/id]}]}
  (when-let [list (get list-table id)]
    (assoc list
      :list/people (mapv (fn [id] {:person/id id}) (:list/people list)))))

(def resolvers [dish-resolver day-menu-resolver menu-resolver the-menu-resolver
                person-resolver list-resolver])
