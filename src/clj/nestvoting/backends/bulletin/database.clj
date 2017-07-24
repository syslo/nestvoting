(ns nestvoting.backends.bulletin.database
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn])
  (:import (java.util UUID)))


(def table-schemas
  [(str
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
     ");")
   (str
     "CREATE TABLE rooms ("
     "    id varchar(36) NOT NULL CONSTRAINT rooms_pk PRIMARY KEY,"
     "    is_listed boolean NOT NULL"
     ");")
   (str
     "CREATE TABLE timestamps ("
     "    id integer NOT NULL CONSTRAINT timestamps_pk PRIMARY KEY AUTOINCREMENT"
     ");")])

(defn create-tables [db]
  (jdbc/db-do-commands db table-schemas))

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

(defn get-rooms [db]
  (let [q ["select * from rooms where is_listed = 1;"]]
    (jdbc/query db q)))


(defn- serialize-message [message]
  (update-in message [:content] str))

(defn- deserialize-message [message]
  (update-in message [:content] edn/read-string))

(defn create-message [db signer message]
  (-> message
      (assoc :id (new-uuid)
             :timestamp (new-timestamp db))
      (signer)
      (serialize-message)
      (doto (->> (jdbc/insert! db :messages)))))

(defn get-messages [db room_id]
  (->> ["select * from messages where room_id = ? order by id;"
        room_id]
       (jdbc/query db)
       (map deserialize-message)))

(defn get-messages-after [db room_id after_timestamp]
  (->> ["select * from messages where room_id = ? and timestamp > ? order by timestamp;"
        room_id after_timestamp]
       (jdbc/query db)
       (map deserialize-message)))
