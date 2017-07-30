(ns nestvoting.backends.bulletin.database
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn])
  (:import (java.util UUID)))


(defn table-schemas [dbtype]
  [(str
     "CREATE TABLE rooms ("
     "    id varchar(36) NOT NULL CONSTRAINT rooms_pk PRIMARY KEY,"
     "    is_listed boolean NOT NULL"
     ");")
   (str
     "CREATE TABLE timestamps ("
     "    id integer NOT NULL CONSTRAINT timestamps_pk PRIMARY KEY AUTOINCREMENT"
     ");")
   (str
     "CREATE TABLE messages ("
     "    id varchar(36) NOT NULL CONSTRAINT messages_pk PRIMARY KEY,"
     "    timestamp integer NOT NULL,"
     "    content text NOT NULL,"
     "    signature text NOT NULL,"
     "    room_id varchar(36) NOT NULL,"
     "    CONSTRAINT messages_timestamp UNIQUE (timestamp),"
     "    CONSTRAINT rooms_messages FOREIGN KEY (room_id)"
     "    REFERENCES rooms (id)"
     "    ON DELETE RESTRICT "
     "    ON UPDATE RESTRICT,"
     "    CONSTRAINT messages_timestamps FOREIGN KEY (timestamp)"
     "    REFERENCES timestamps (id)"
     "    ON DELETE RESTRICT "
     "    ON UPDATE RESTRICT"
     ");")])

(defn create-tables [db]
  (jdbc/db-do-commands db (table-schemas (keyword (:dbtype db)))))

(defn- new-uuid []
  (.toString (UUID/randomUUID)))

(defn- new-timestamp [db]
  (-> (jdbc/insert! db :timestamps {:id nil})
      (first)
      ((keyword "last_insert_rowid()"))))


(defn create-room [db room]
  (-> room
      (assoc :id (new-uuid))
      (doto (->> (jdbc/insert! db :rooms)))))

(defn get-rooms [db & {:keys [limit]}]
  (->> [(str "select * from rooms where is_listed = 1"
             (if (some? limit) " limit ?" ""))
        limit]
       (filter some?)
       (vec)
       (jdbc/query db)))

(defn- serialize-message [message]
  (update-in message [:content] str))

(defn- deserialize-message [message]
  (update-in message [:content] edn/read-string))

(defn create-message [db signer message]
  (-> message
      (assoc :id (new-uuid)
             :timestamp (new-timestamp db))
      (signer)
      (doto (serialize-message)
            (->> (jdbc/insert! db :messages)))))

(defn get-messages [db room_id & {:keys [after limit]}]
  (->> [(str "select * from messages where room_id = ?"
             (if (some? after) " and timestamp > ?" "")
             " order by timestamp"
             (if (some? limit) " limit ?" ""))
        room_id after limit]
       (filter some?)
       (vec)
       (jdbc/query db)
       (map deserialize-message)))

(defn get-room [db id]
  (->> ["select * from rooms where id = ?" id]
       (jdbc/query db)
       first))

(defn get-message [db id]
  (->> ["select * from messages where id = ?" id]
       (jdbc/query db)
       first
       (deserialize-message)))
