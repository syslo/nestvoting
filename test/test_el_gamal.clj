(ns test-el-gamal
  (:require [nestvoting.math.group :as G]
            [nestvoting.math.el-gamal :as el-gamal]
            [test-groups :refer [random-cyclic-group-elem]])
  (:use clojure.test))


(defn basic-encryption [G]
  (let [{:keys [public secret] } (el-gamal/gen-key G)
        in-elem (random-cyclic-group-elem G)
        cipher (el-gamal/encrypt G public in-elem)
        out-elem (el-gamal/decrypt G secret cipher)]
    (is (G/= G in-elem out-elem))))

(defn homomorfic-encryption [G]
  (let [{:keys [public secret] } (el-gamal/gen-key G)
        ins [0 1 2 3 4 5 6 7 8 9]
        ciphers (map #(el-gamal/encrypt-homo G public %))
        cipher (el-gamal/combine-ciphers G ciphers)
        out-elem-ok (el-gamal/decrypt-homo G secret cipher 47)
        out-elem-bad (el-gamal/decrypt-homo G secret cipher 42)]
    (is (= out-elem-ok 45))
    (is (= out-elem-bad nil))))

(defn shared-encryption [G]
  (let [keys (repeatedly 10 (fn [] (el-gamal/gen-key G)))
        public (el-gamal/combine-pks G (map :public keys))
        in-elem (random-cyclic-group-elem G)
        cipher (el-gamal/encrypt G public in-elem)
        out-elems (map #(el-gamal/decrypt G (:secret %) cipher) keys)
        out-elem (el-gamal/combine-plaintexts G out-elems)]
    (is (G/= G in-elem out-elem))))

(deftest test-encryption-cycles
  (map
    (fn [G]
      (basic-encryption G)
      (homomorfic-encryption G)
      (shared-encryption G))
    [(G/effective-random-group)
     (G/schnorr-generate 47 42)]))
