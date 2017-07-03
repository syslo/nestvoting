(ns nestvoting.math.algorithm
  (:require [nestvoting.math.bigint :as N]))


(defn associative-pow [op e x n]
  (loop [x x
         n n
         r e]
    (if (N/= n N/_0)
      r
      (let [[nq nm] (N/quotmod n N/_2)]
        (recur (op x x)
               nq
               (if (N/= nm N/_0) r (op r x)))))))

(defn mod-* [x y p] (second (N/quotmod (N/* x y) p)))

(defn mod-pow [x n p] (associative-pow #(mod-* %1 %2 p) N/_1 x n))


(defn random
  ([min max] (N/+ min (random (N/- max min))))
  ([max] (second (N/quotmod (N/random-bytes (+ 2 (count (N/str max :base 256)))) max))))


(defn miller-rabin [p & {:keys [iterations]
                         :or {iterations 32}}]
  (and (N/< N/_1 p)
    (or (N/= N/_2 p) (N/= N/_3 p)
      (let [p_1 (N/- p N/_1)
            p_2 (N/- p N/_2)
            [r d] (loop [r 0 d p_1]
                    (let [[dq dr] (N/quotmod d N/_2)]
                      (if (N/= dr N/_1)
                        [r d]
                        (recur (inc r) dq))))]
        (and (> r 0))
        (every? true?
            (repeatedly iterations
              (fn []
                (let [a (random N/_2 p_2)
                      x (mod-pow a d p)]
                  (or (N/= x N/_1) (N/= x p_1)
                    (->> (iterate (fn [x] (mod-* x x p)) x)
                         (take r)
                         (next)
                         (filter #(or (N/= % N/_1) (N/= % p_1)))
                         (first)
                         (#(and % (N/= % p_1)))))))))))))
