(ns nestvoting.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [hiccup.page :refer [include-js include-css html5]]
            [ring.middleware.json :refer [wrap-json-response]]
            [nestvoting.middleware :refer [wrap-middleware]]
            [nestvoting.config :refer [profile frontend-config]]))

(def mount-target
  [:div#app
      [:h3 "ClojureScript has not been compiled!"]
      [:p "please run "
       [:b "lein figwheel"]
       " in order to start the compiler"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (= profile :dev) "/css/site.css" "/css/site.min.css"))
   [:script {:src "http://peterolson.github.com/BigInteger.js/BigInteger.min.js"}]
   [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/jsSHA/2.2.0/sha256.js"}]])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defroutes routes
  (GET "/" [] (loading-page))
  (GET "/about" [] (loading-page))

  (GET "/config.json" [] (frontend-config))
  
  (resources "/")
  (not-found "Not Found"))

(def app (-> #'routes
             wrap-middleware
             wrap-json-response))
