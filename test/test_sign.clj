(ns test-sign
  (:require [clojure.core.async :as async :refer [<!!]]
            [nestvoting.math.signature :as signature])
  (:use clojure.test))


(deftest basic-cycle []
  (testing "Basic lifecycle of RSA"
    (let [{:keys [public private]} (<!! (signature/generate-rsa-key-pair))
          public (<!! (signature/import-public-rsa
                        (<!! (signature/export-public-rsa public))))
          message "This is a test message!"
          s (<!! (signature/sign private message))]
       (is (<!! (signature/verify public message s)))
       (is (not (<!! (signature/verify public "This is a different message" s))))
       (is (not (<!! (signature/verify public message (str "FAKE" (subs s 4)))))))))
