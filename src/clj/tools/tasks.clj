(ns tools.tasks
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str])
  (:gen-class))


(defn execute [task input]
  (let [{opts-spec :options
         :keys [handler
                summary
                description
                auto-help?
                in-order]
         :or {auto-help? true}} task
        {opts-old :options
         args-old :arguments} input
        opts-spec (cond-> opts-spec
                          auto-help? (concat [["-h" "--help" "Show this help message"
                                               :id :help]]))
        {opts-new :options
         args-new :arguments
         opts-help :summary
         :keys [errors]} (parse-opts args-old opts-spec :in-order in-order)
        opts-new (merge opts-old opts-new)
        help-text (str summary "\n"
                       "\n" description "\n"
                       "\n" "Options:" "\n" opts-help "\n")]
    (if errors
      (do (println (str "Error:" "\n"
                        (str/join "\n" errors)))
          (System/exit 22)))
    (if (and auto-help? (:help opts-new))
      (do (println help-text)
          (System/exit 0)))
    (handler {:options opts-new
              :arguments args-new
              :help-text help-text})))


(defn main-fn [task]
  (fn [& args] (execute task {:arguments args})))


(defn group [summary tasks]
  {:in-order true
   :summary summary
   :description
     (str "Subcommands:" "\n"
          (->> tasks
               (map (fn [[k v]] (str "- " (name k) " (" (:summary v) ")")))
               (str/join "\n")))
   :handler
     (fn [input]
       (let [task-name (-> input :arguments first)
             task (-> task-name keyword tasks)
             input (update input :arguments next)]
         (if-not task-name
           (do (println (:help-text input))
               (System/exit 0)))
         (if-not task
           (do (println (:help-text input))
               (System/exit 22)))
         (execute task input)))})
