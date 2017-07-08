(ns tests.math.signature
  (:require [clojure.core.async :as async :refer [<!!]]
            [test-helpers.math.signature :refer [construct-signature-example
                                                 validate-signature-example
                                                 signature-examples]])
  (:use clojure.test))


(deftest sign-and-verify []
  (testing "Signing and verifing message"
    (let [example (<!! (construct-signature-example "This is a test message"))
          verified-original (<!! (validate-signature-example example))
          verified-bad-msg (<!! (validate-signature-example
                                  (assoc-in example [:msg] "This is a different message")))
          verified-bad-sgn (<!! (validate-signature-example
                                  (update-in example [:sgn] #(str "FAKE" (subs % 4)))))]
       (is verified-original)
       (is (not verified-bad-msg))
       (is (not verified-bad-sgn)))))


(deftest verify-examples []
  (testing "Verifing prepared examples"
    (let [results (<!! (async/map vector (map validate-signature-example signature-examples)))]
      (is (every? true? results)))))
