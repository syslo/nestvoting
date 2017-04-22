(ns nestvoting.math.bigint
  (:import [java.math.BigInteger]))

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
