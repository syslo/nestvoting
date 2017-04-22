(ns test-groups
  (:require [nestvoting.math.bigint :as N]
            [nestvoting.math.group :as G]
            [nestvoting.math.algorithm :as algo])
  (:use clojure.test))

(defn generic-group-test [G get-elem]
  (let [[x1 x2 x3] (repeatedly 3 get-elem)]
    (is (G/= G (G/e G) (G/* G (G/e G) (G/e G))))
    (is (G/= G x1 (G/* G (G/e G) x1)))
    (is (G/= G (G/e G) (G/* G x2 (G/inv G x2))))
    (is (G/= G (G/e G) (G/div G x3 x3)))
    (is (G/= G (G/* G (G/* G x1 x2) x3) (G/* G x1 (G/* G x2 x3))))))

(defn cyclic-group-test [G n]
  (let [get-elem #(G/pow G (G/g G) (algo/random N/_1 (N/- (G/order G) N/_1)))]
    (doall (repeatedly n #(generic-group-test G get-elem))))
  (is (G/= G (G/pow G (G/g G) (G/order G)) (G/e G))))


(deftest efective-random-group []
  (let [G (G/effective-random-group)]
    (cyclic-group-test G 20)))

(deftest small-schnorr-group []
  (doall
    (repeatedly 10
      #(cyclic-group-test (G/schnorr-generate 47 42) 20))))
