(ns nestvoting.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [cljs.core.async :refer [<!]]
              [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [nestvoting.config :as config :refer [in-config]]))


;; -------------------------
;; Views



(defn home-page []
  [:div [:h2 "Welcome to nestvoting"]
    [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h2 "About nestvoting"]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init-app! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))

(defn init! []
  (go (<! (config/init!))
      (init-app!)))
