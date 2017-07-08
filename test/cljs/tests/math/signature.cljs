(ns tests.math.signature
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cljs.test :refer [testing deftest is async]])
  (:require [cljs.core.async :as async :refer [<!]]
            [test-helpers.math.signature :refer [construct-signature-example
                                                 validate-signature-example
                                                 signature-examples]]))

(deftest sign-and-verify []
  (async done
    (testing "Signing and verifing message"
      (go
        (let [example (<! (construct-signature-example "This is a test message"))
              verified-original (<! (validate-signature-example example))
              verified-bad-msg (<! (validate-signature-example
                                      (assoc-in example [:msg] "This is a different message")))
              verified-bad-sgn (<! (validate-signature-example
                                      (update-in example [:sgn] #(str "FAKE" (subs % 4)))))]
          (is verified-original)
          (is (not verified-bad-msg))
          (is (not verified-bad-sgn))
          (done))))))

(deftest verify-examples []
  (async done
    (testing "Verifing prepared examples"
      (go
        (let [results (<! (async/map vector (map validate-signature-example signature-examples)))]
          (is (every? true? results))
          (done))))))
