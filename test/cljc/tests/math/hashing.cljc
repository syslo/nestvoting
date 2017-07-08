(ns tests.math.hashing
  (:require [nestvoting.math.hashing :refer [secure-hash]])
  #?(:clj (:use clojure.test))
  #?(:cljs (:require-macros [cljs.test :refer [deftest is]])))


(deftest simple-hash-test []
  (is (secure-hash :sha2-256 "This is a test message.")
      "0668b515bfc41b90b6a90a6ae8600256e1c76a67d17c78a26127ddeb9b324435"))
