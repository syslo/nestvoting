(ns nestvoting.math.bigint
  (:import (java.math BigInteger)
           (java.security SecureRandom)))

(defn val [s & {:keys [base]}]
  (bigint (if base (new BigInteger s base) s)))

(defn str [x & {:keys [base] :or {base 10}}]
  (.toString (.toBigInteger x) base))

(defn + [x y] (clojure.core/+ x y))
(defn - [x y] (clojure.core/- x y))
(defn * [x y] (clojure.core/* x y))
(defn quotmod [x y] [(quot x y) (mod x y)])

(defn = [x y] (clojure.core/= x y))
(defn < [x y] (clojure.core/< x y))

(def _0 0N)
(def _1 1N)
(def _2 2N)
(def _3 3N)


(defn- aupdate-byte [array index fun]
  (aset-byte array index (fun (aget array index)))
  array)

(defn random-bytes [n & {:keys [first-bit last-bit]}]
  (-> (byte-array n)
      (doto (->> (.nextBytes (new SecureRandom))))
      (cond->
        (some? first-bit) (aupdate-byte 0 #(clojure.core/+ (mod % 128) (if first-bit -128 0)))
        (some? last-bit) (aupdate-byte (clojure.core/- n 1) #(clojure.core/- % (mod % 2) (if last-bit -1 0))))
      (->> (new BigInteger 1))))
