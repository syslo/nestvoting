(ns nestvoting.math.bigint)


(defn val [s & {:keys [base] :or {base 10}}]
  (.bigInt js/window s base))

(defn str [x & {:keys [base] :or {base 10}}]
  (.toString x base))

(defn + [x y] (.add x y))
(defn - [x y] (.subtract x y))
(defn * [x y] (.multiply x y))
(defn quotmod [x y] (let [r (.divmod x y)]
                      [(.-quotient r) (.-remainder r)]))

(defn = [x y] (.eq x y))
(defn < [x y] (.lt x y))

(def _0 (val 0))
(def _1 (val 1))
(def _2 (val 2))
(def _3 (val 3))
