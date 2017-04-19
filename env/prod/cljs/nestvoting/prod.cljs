(ns nestvoting.prod
  (:require [nestvoting.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
