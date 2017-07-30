(ns tests.backends.bulletin
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core.async :as async :refer [<!!]]
            [nestvoting.math.signature :as math.signature]
            [nestvoting.protocol.signature :as protocol.signature]
            [nestvoting.backends.bulletin.database :as db]
            [nestvoting.backends.bulletin.routes :as routes]
            [compojure.core])
  (:use clojure.test))


(deftest database-test []
  (testing "the bulletin database operations"
    (jdbc/with-db-connection [db {:dbtype "sqlite"
                                  :connection-uri "jdbc:sqlite::memory:"}]
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
        (is (vec (db/get-room db (:id room_unlisted)))
            room_unlisted)
        (is (vec (db/get-messages db (:id room_unlisted)))
            [msg1 msg2])
        (is (vec (db/get-messages db (:id room_unlisted) :limit 1))
            [msg1])
        (is (vec (db/get-messages db (:id room_unlisted) :after (:timestamp msg1)))
            [msg2])
        (is (vec (db/get-message db (:id msg2)))
            msg2)))))

(deftest routes-test []
  (testing "the bulletin routes"
    (jdbc/with-db-connection [db {:dbtype "sqlite"
                                  :connection-uri "jdbc:sqlite::memory:"}]
      (db/create-tables db)
      (let [{:keys [public] :as key-pair} (<!! (math.signature/generate-rsa-key-pair))
            config {:db db
                    :rsa-key-pair (<!! (math.signature/export-rsa-pair key-pair))}
            handler (routes/build config)
            room_listed (-> {:request-method :post
                             :uri "/rooms"
                             :body {:is_listed false}}
                            handler
                            :body)
            room_unlisted (-> {:request-method :post
                               :uri "/rooms"
                               :body {:is_listed true}}
                              handler
                              :body)
            msg1 (-> {:request-method :post
                      :uri (str "/rooms/" (:id room_unlisted) "/messages")
                      :body {:test "1"}}
                     handler
                     :body)
            msg2 (-> {:request-method :post
                      :uri (str "/rooms/" (:id room_unlisted) "/messages")
                      :body {:test "2"}}
                     handler
                     :body)
            msg1-ok? (<!! (protocol.signature/verify-message public msg1))
            msg2-ok? (<!! (protocol.signature/verify-message public msg2))]
        (is msg1-ok?)
        (is msg2-ok?)
        (is [room_listed]
            (-> {:request-method :get
                 :uri "/rooms"}
                handler
                :body))
        (is room_unlisted
            (-> {:request-method :get
                 :uri (str "/room/" (:id room_unlisted))}
                handler
                :body))
        (is [msg1 msg2]
            (-> {:request-method :get
                 :uri (str "/room/" (:id room_unlisted) "/messages")}
                handler
                :body))
        (is [msg1]
            (-> {:request-method :get
                 :uri (str "/room/" (:id room_unlisted) "/messages?limit=" 1)}
                handler
                :body))
        (is [msg2]
            (-> {:request-method :get
                 :uri (str "/room/" (:id room_unlisted) "/messages?last_seen=" (:timestamp msg1))}
                handler
                :body))
        (is msg2
            (-> {:request-method :get
                 :uri (str "/message/" (:id room_unlisted))}
                handler
                :body))))))


