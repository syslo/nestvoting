(ns nestvoting.config
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! chan close!]]
            [cljs-http.client :as http]))


(def config (atom {}))

(defn in-config [& path]
   (get-in @config path))

(defn init! []
  (go (reset! config (:body (<! (http/get "/config.json"))))))
