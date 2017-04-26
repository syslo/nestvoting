(ns nestvoting.math.el-gamal
  (:require [nestvoting.math.bigint :as N]
            [nestvoting.math.group :as G]
            [nestvoting.math.algorithm :refer [random]]))


(defn gen-key [G]
  (let [x (random N/_2 (N/- (G/order G) N/_1))
        y (G/pow G (G/g G) x)]
    {:public (G/serialize-elem G x)
     :secret (N/str y)}))

(defn encrypt [G public elem]
  (let [y (random N/_1 (N/- (G/g G) N/_1))
        c1 (G/pow G (G/g G) y)
        s (G/pow G (G/deserialize-elem G public) y)
        c2 (G/* G s elem)]
    {:c1 (G/serialize-elem G c1)
     :c2 (G/serialize-elem G c2)}))

(defn decrypt [G secret cipher]
  (let [c1 (G/deserialize-elem G (:c1 cipher))
        c2 (G/deserialize-elem G (:c2 cipher))
        s (G/pow G c1 (N/val secret))]
    (G/div G c2 s)))

(defn encrypt-homo [G public n]
  (let [elem (G/pow G (G/g G) (N/val n))]
    (encrypt G public elem)))

(defn decrypt-homo [G private cipher max]
  (let [elem (decrypt G private cipher)]
    (->> [0 (G/e G)]
         (iterate (fn [[n a]] [(inc n) (G/* G (G/g G) a)]))
         (take (+ max 1))
         (filter #(G/= G elem %))
         (ffirst))))

(defn combine-ciphers [G ciphers]
  (reduce
    (fn [result key]
      (assoc result key
         (->> ciphers
              (map key)
              (map #(G/deserialize-elem G %))
              (G/*s G)
              (G/serialize-elem G))))
    {} [:c1 :c2]))


(defn combine-pks [G keys]
  (->> keys (map #(G/deserialize-elem G %)) (G/*s G) (G/serialize-elem G)))

(def combine-plaintexts G/*s)
