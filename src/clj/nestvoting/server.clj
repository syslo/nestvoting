(ns nestvoting.server
  (:require [nestvoting.handler :refer [app]]
            [nestvoting.config :refer [in-config]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (run-jetty app {:port (in-config :backend :port) :join? false}))
