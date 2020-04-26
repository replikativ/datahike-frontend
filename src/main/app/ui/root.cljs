(ns app.ui.root
  (:require
    [app.model.session :as session]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [app.mutations :as api]
    [app.application :refer [SPA]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [taoensso.timbre :as log]
;;    ["reactstrap" :as rs :refer [Alert Button]]
    ;;["react-bootstrap" :as rs :refer [Alert Button]]
    ))



(defn field [{:keys [label valid? error-message] :as props}]
  (let [input-props (-> props (assoc :name label) (dissoc :label :valid? :error-message))]
    (div :.ui.field
      (dom/label {:htmlFor label} label)
      (dom/input input-props)
      (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
        error-message))))

(defsc Session
  "Session representation. Used primarily for server queries. On-screen representation happens in Login component."
  [this {:keys [:session/valid? :account/name] :as props}]
  {:query         [:session/valid? :account/name]
   :ident         (fn [] [:component/id :session])
   :pre-merge     (fn [{:keys [data-tree]}]
                    (merge {:session/valid? false :account/name ""}
                      data-tree))
   :initial-state {:session/valid? false :account/name ""}})

(def ui-session (comp/factory Session))


(defsc Login [this {:account/keys [email]
                    :ui/keys      [error open?] :as props}]
  {:query         [:ui/open? :ui/error :account/email
                   {[:component/id :session] (comp/get-query Session)}
                   [::uism/asm-id ::session/session]]
   :css           [[:.floating-menu {:position "absolute !important"
                                     :z-index  1000
                                     :width    "300px"
                                     :right    "0px"
                                     :top      "50px"}]]
   :initial-state {:account/email "" :ui/error ""}
   :ident         (fn [] [:component/id :login])}
  (let [current-state (uism/get-active-state this ::session/session)
        {current-user :account/name} (get props [:component/id :session])
        initial?      (= :initial current-state)
        loading?      (= :state/checking-session current-state)
        logged-in?    (= :state/logged-in current-state)
        {:keys [floating-menu]} (css/get-classnames Login)
        password      (or (comp/get-state this :password) "")] ; c.l. state for security
    (dom/div
      (when-not initial?
        (dom/div :.right.menu
          (if logged-in?
            (dom/button :.item
              {:onClick #(uism/trigger! this ::session/session :event/logout)}
              (dom/span current-user) ent/nbsp "Log out")
            (dom/div :.item {:style   {:position "relative"}
                             :onClick #(uism/trigger! this ::session/session :event/toggle-modal)}
              "Login"
              (when open?
                (dom/div :.four.wide.ui.raised.teal.segment {:onClick (fn [e]
                                                                        ;; Stop bubbling (would trigger the menu toggle)
                                                                        (evt/stop-propagation! e))
                                                             :classes [floating-menu]}
                  (dom/h3 :.ui.header "Login")
                  (div :.ui.form {:classes [(when (seq error) "error")]}
                    (field {:label    "Email"
                            :value    email
                            :onChange #(m/set-string! this :account/email :event %)})
                    (field {:label    "Password"
                            :type     "password"
                            :value    password
                            :onChange #(comp/set-state! this {:password (evt/target-value %)})})
                    (div :.ui.error.message error)
                    (div :.ui.field
                      (dom/button :.ui.button
                        {:onClick (fn [] (uism/trigger! this ::session/session :event/login {:username email
                                                                                             :password password}))
                         :classes [(when loading? "loading")]} "Login"))
                    (div :.ui.message
                      (dom/p "Don't have an account?")
                      (dom/a {:onClick (fn []
                                         (uism/trigger! this ::session/session :event/toggle-modal {})
                                         (dr/change-route this ["signup"]))}
                        "Please sign up!"))))))))))))

(def ui-login (comp/factory Login))


(defsc Dish [this {:dish/keys [id name] :as props} {:keys [onDelete]}] ; (2)
  {:query         [:dish/id :dish/name]
   :ident         (fn [] [:dish/id (:dish/id props)])
   :initial-state (fn [{:keys [id name] :as params}] {:dish/id id :dish/name name})}
    (dom/h5 (str name) (dom/button {:onClick #(onDelete id)} "X"))) ; (2)

;; :keyfn is something related to react: it gives a key to each entry
(def ui-dish (comp/factory Dish {:keyfn :dish/name}))

(defsc DayMenu [this {:day-menu/keys [id day starter dish-list] :as props}]
  {:query [:day-menu/id :day-menu/day :day-menu/starter {:day-menu/dish-list (comp/get-query Dish)}]
   :ident         (fn [] [:day-menu/id (:day-menu/id props)])
   :initial-state (fn [{:keys [id day starter dish-list]}]
                    {:day-menu/id        id
                     :day-menu/day       day
                     :day-menu/starter   starter
                     :day-menu/dish-list [(comp/get-initial-state Dish {:id 1 :name "Poulette sauvage"})
                                          (comp/get-initial-state Dish {:id 2 :name "Sauce Curry"})
                                          (comp/get-initial-state Dish {:id 3 :name "Riz"})]})}
  ;; (fn [name] (comp/transact! this [(api/delete-dish {:list/id id :person/id person-id})]))
  (let [delete-dish (fn [dish-id] (comp/transact! this [(api/delete-dish {:day-menu-id id :dish-id dish-id})]))]
    (dom/div
      (dom/h2 (str day))
      (dom/h4 (str starter))
      ;; computed allows to recompute what we transfer as props. Here we add 'delete-dish
      ;; to the props
      (map (fn [dish] (ui-dish (comp/computed dish {:onDelete delete-dish}))) dish-list))))

(def ui-day-menu (comp/factory DayMenu {:keyfn :day-menu/day}))



(defsc Menu [this {:menu/keys [id week-desc day-menu-list]}]
  {:query [:menu/id :menu/week-desc {:menu/day-menu-list (comp/get-query DayMenu)}]
   :ident         (fn [] [:menu/id id])
   :route-segment ["menu"]
   :initial-state (fn [{:keys [id week-desc]}]
                    ;;(println "aaaaaaa" id)
                    {:menu/id id
                     :menu/week-desc week-desc
                     :menu/day-menu-list [(comp/get-initial-state DayMenu {:id 1 :day "Mondayyyy" :starter "Tomate Mozza"})
                                          (comp/get-initial-state DayMenu {:id 2 :day "Tuesday" :starter "Crevettes"})
                                          ]})}
  (dom/div
    (dom/h2 "Menu startsss here: ")
    ;;(Button "bager")
;;    (Alert {:color "primary" :value "Alertaaa"})
    (dom/h1 week-desc)
    ;;(println (str "menes: " menus)) ;; <- would work but the component view would not appear
    (map ui-day-menu day-menu-list)))

(def ui-menu (comp/factory Menu {:keyfn :menu/week-desc}))

(def navigation
  (dom/nav
     :.navbar.navbar-expand-lg.navbar-dark.fixed-top
     {:id "mainNav"}
     (dom/div
       :.container
       (dom/a
         :.navbar-brand.js-scroll-trigger
         {:href "#page-top"}
         "Le Petit-Bistrot")
       (dom/button
         :.navbar-toggler.navbar-toggler-right
         {:type "button",
          :data-toggle "collapse",
          :data-target "#navbarResponsive",
          :aria-controls "navbarResponsive",
          :aria-expanded "false",
          :aria-label "Toggle navigation"}
         "Menu"
         (dom/i :.fas.fa-bars))
       (dom/div
         :.collapse.navbar-collapse
         {:id "navbarResponsive"}
         (dom/ul
           :.navbar-nav.text-uppercase.ml-auto
           (dom/li
             :.nav-item
             (dom/a
               :.nav-link.js-scroll-trigger
               {:href "#Menu"
                :onClick #(dr/change-route SPA ["main"])}
               "Menu de la Semaine"))
           (dom/li
             :.nav-item
             (dom/a
               :.nav-link.js-scroll-trigger
               {:href "#petite_carte"}
               "Petite Carte"))
           (dom/li
             :.nav-item
             (dom/a
               :.nav-link.js-scroll-trigger
               {:href "#contact"}
               "Contact"))
           (dom/li
             :.nav-item
             (dom/a
               :.nav-link.js-scroll-trigger
               {:href "#Menu"
                :onClick (fn [] (dr/change-route SPA ["edit"]))}
               "Modifier")))))))

(def header
  (dom/header
    :.masthead
    (dom/div
      :.container
      (dom/div
        :.intro-text
        (dom/div :.intro-lead-in "Bienvenue au Petit-Bistrot")))))


(defsc Form [this {:menu/keys [id week-desc] :as props}]
  {:query [:menu/id :menu/week-desc fs/form-config-join]
   :ident (fn [] [:menu-form/id :form]) ;; With this (fn ...) version, we don't get the ':form does not appear in your arguments' error message.
   :form-fields #{:week-desc}
   :route-segment ["edit"]
   :initial-state {}}
  [navigation
   header
   (dom/div
     (dom/input {:value    week-desc
                 ;; Without the onChange the field is immutable
                 :onChange #(m/set-string! this :menu/week-desc :event %)})
     (dom/button
       {:onClick #(comp/transact! this
                    `[(api/submit-week-desc ~{:week-desc week-desc
                                              :diff (fs/dirty-fields props true)})])}
       "Submit"))])

(def ui-form (comp/factory Form))


;; Root's initial state becomes the entire app's initial state!
(defsc PBRoot [this {:keys [menu login] :as params}]
  {:query [{:menu (comp/get-query Menu)}
           {:login (comp/get-query Login)}]
   :initial-state  (fn [_] {;; TODO: fix the map of args here
                            :menu (comp/get-initial-state Menu {:id 1 :week-desc "Test"})
                            :login {}})}
  [navigation
   header
   ;; The heart of the app
   (dom/div
     ;; TODO: peut-'etre que le router n'est pas initialise quand on commence l'app et c'est pour ca qu'il ne marche pas
     ;;(ui-main-router router)
     (print "menus is: " menu)

     (ui-login login)
     (ui-menu menu))])


(def ui-pb-root (comp/factory PBRoot))


(defsc Main [this {:keys [router menu] :as props}]
  {:query         [:router :menu]
   :initial-state (fn [_]
                    (comp/get-initial-state PBRoot))
   :ident         (fn [] [:component/id :main])
   :route-segment ["main"]}
  (div :.ui.container.segment
    (print "Mains's props: -......" props)
    (ui-pb-root props)))


(defsc SignupSuccess [this props]
  {:query         ['*]
   :initial-state {}
   :ident         (fn [] [:component/id :signup-success])
   :route-segment ["signup-success"]}
  (div
    (dom/h3 "Signup Complete!")
    (dom/p "You can now log in!")))

(defsc Signup [this {:account/keys [email password password-again] :as props}]
  {:query             [:account/email :account/password :account/password-again fs/form-config-join]
   :initial-state     (fn [_]
                        (fs/add-form-config Signup
                          {:account/email          ""
                           :account/password       ""
                           :account/password-again ""}))
   :form-fields       #{:account/email :account/password :account/password-again}
   :ident             (fn [] session/signup-ident)
   :route-segment     ["signup"]
   :componentDidMount (fn [this]
                        (comp/transact! this [(session/clear-signup-form)]))}
  (let [submit!  (fn [evt]
                   (when (or (identical? true evt) (evt/enter-key? evt))
                     (comp/transact! this [(session/signup! {:email email :password password})])
                     (log/info "Sign up")))
        checked? (fs/checked? props)]
    (div
      (dom/h3 "Signup")
      (div :.ui.form {:classes [(when checked? "error")]}
        (field {:label         "Email"
                :value         (or email "")
                :valid?        (session/valid-email? email)
                :error-message "Must be an email address"
                :autoComplete  "off"
                :onKeyDown     submit!
                :onChange      #(m/set-string! this :account/email :event %)})
        (field {:label         "Password"
                :type          "password"
                :value         (or password "")
                :valid?        (session/valid-password? password)
                :error-message "Password must be at least 8 characters."
                :onKeyDown     submit!
                :autoComplete  "off"
                :onChange      #(m/set-string! this :account/password :event %)})
        (field {:label         "Repeat Password" :type "password" :value (or password-again "")
                :autoComplete  "off"
                :valid?        (= password password-again)
                :error-message "Passwords do not match."
                :onChange      #(m/set-string! this :account/password-again :event %)})
        (dom/button :.ui.primary.button {:onClick #(submit! true)}
          "Sign Up")))))

(defsc Settings [this {:keys [:account/time-zone :account/real-name] :as props}]
  {:query         [:account/time-zone :account/real-name]
   :ident         (fn [] [:component/id :settings])
   :route-segment ["settings"]
   :initial-state {}}
  (div :.ui.container.segment
    (h3 "Settings")
    (div
      (p (b "Name: ") real-name)
      (p (b "Time Zone: ") time-zone))))

(dr/defrouter TopRouter [this props]
  {:router-targets [Main Signup SignupSuccess Settings Form]})

(def ui-top-router (comp/factory TopRouter))

(defsc TopChrome [this {:root/keys [router current-session login]}]
  {:query         [{:root/router (comp/get-query TopRouter)}
                   {:root/current-session (comp/get-query Session)}
                   [::uism/asm-id ::TopRouter]
                   {:root/login (comp/get-query Login)}]
   :ident         (fn [] [:component/id :top-chrome])
   :initial-state {:root/router          {}
                   :root/login           {}
                   :root/current-session {}}}
  (let [current-tab (some-> (dr/current-route this this) first keyword)]
    (ui-top-router router)
    ;; (div :.ui.container
    ;;   (div :.ui.secondary.pointing.menu
    ;;     (dom/a :.item {:classes [(when (= :main current-tab) "active")]
    ;;                    :onClick (fn [] (dr/change-route this ["main"]))} "Main")
    ;;     (dom/a :.item {:classes [(when (= :settings current-tab) "active")]
    ;;                    :onClick (fn [] (dr/change-route this ["settings"]))} "Settings")
    ;;     (dom/a :.item {:classes [(when (= :main current-tab) "active")]
    ;;                    :onClick (fn [] (dr/change-route this ["edit"]))} "edit")
    ;;     (div :.right.menu
    ;;       (ui-login login)))
    ;;   (div :.ui.grid
    ;;     (div :.ui.row
    ;;       (ui-top-router router))))
    ))

(def ui-top-chrome (comp/factory TopChrome))

(defsc Root [this {:root/keys [top-chrome]}]
  {:query         [{:root/top-chrome (comp/get-query TopChrome)}]
   :initial-state {:root/top-chrome {}}}
  (ui-top-chrome top-chrome))
