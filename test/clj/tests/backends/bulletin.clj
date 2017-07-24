(ns tests.backends.bulletin
  (:require [clojure.java.jdbc :as jdbc]
            [nestvoting.backends.bulletin.database :as db])
  (:use clojure.test))


(deftest database-test []
  (testing "The database operations"
    (jdbc/with-db-connection [db "jdbc:sqlite::memory:"]
      (db/create-tables db)
      (let [signer #(assoc % :signature "fake")
            room_listed (db/create-room db {:is_listed true})
            room_unlisted (db/create-room db {:is_listed false})
            msg1 (db/create-message db signer
                    {:content {:test "1"} :room_id (:id room_unlisted)})
            msg2 (db/create-message db signer
                    {:content {:test "2"} :room_id (:id room_unlisted)})]
        (is (vec (db/get-rooms db))
            [room_listed])
        (is (vec (db/get-messages db (:id room_unlisted)))
            [msg1 msg2])
        (is (vec (db/get-messages-after db (:id room_unlisted) (:timestamp msg1)))
            [msg2])))))
