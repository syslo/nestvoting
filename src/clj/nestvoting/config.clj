(ns nestvoting.config
  (:require [aero.core :refer [read-config]]
            [ring.util.http-response :refer [ok]]
            [clojure.java.io :as io]))


(def profile (keyword (or (System/getenv "PROFILE") "dev")))

(def config
  (read-config (io/resource "config.edn") {:profile profile}))

(defn in-config [& path] (get-in config path))

(defn frontend-config []
  (ok (-> (in-config)
          (dissoc :backend)
          (assoc-in [:server :baseurl] ""))))
