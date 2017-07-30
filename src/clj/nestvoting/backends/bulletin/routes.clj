(ns nestvoting.backends.bulletin.routes
  (:require [clojure.core.async :as async :refer [<!!]]
            [nestvoting.protocol.signature :refer [sign-message]]
            [nestvoting.math.signature :refer [import-rsa-pair]]
            [nestvoting.backends.bulletin.database :as db]
            [compojure.core :refer [routes GET POST]]
            [ring.util.http-response :refer [ok not-found]]))


(defn build [config]
  (let [db (-> config :db)
        {private-key :private} (-> config :rsa-key-pair import-rsa-pair <!!)]
    (routes
      (GET "/rooms" [limit]
        (ok (db/get-rooms db :limit limit)))

      (GET "/rooms/:room-id" [room-id]
        (let [room (db/get-room db room-id)]
          (if (some? room)
            (ok room)
            (not-found))))

      (GET "/rooms/:room-id/messages" [room-id limit last_seen]
        (ok (db/get-messages db room-id :limit limit :after last_seen)))

      (GET "/messages/:message-id" [message-id]
        (let [msg (db/get-message db message-id)]
          (if (some? msg)
            (ok msg)
            (not-found))))

      (POST "/rooms" {:keys [body]}
        (-> body
            (select-keys [:is_listed])
            (->> (db/create-room db))
            (ok)))

      (POST "/rooms/:room-id/messages" [room-id :as {:keys [body]}]
        (let [room (db/get-room db room-id)]
          (if (some? room)
            (-> {:content body
                 :room_id room-id}
                (->> (db/create-message db #(<!! (sign-message private-key %))))
                (ok))
            (not-found)))))))
