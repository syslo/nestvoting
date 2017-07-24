(ns nestvoting.manage
  (:require [tools.tasks :as tasks]
            [nestvoting.backends.bulletin.database :as db]
            [nestvoting.config :refer [in-config]])
  (:gen-class))

; run with: java -cp nestvoting.jar nestvoting.manage

(def -main
  (tasks/main-fn
    (tasks/group "Nestvoting management"
      {:hello
        {:summary "Greets you"
         :description "It's nice to have a polite application"
         :options [["-n" "--name N" "Your name"
                    :id :name :default "stranger"]]
         :handler
           (fn [{{:keys [name]} :options}]
             (println (str "Hello " name "!")))}
       :init-db
        {:summary "Initializes the database"
         :description "Creates the necessary tables in the configured database. The database should be empty."
         :handler
           (fn [_]
             (db/create-tables (in-config :database)))}})))
