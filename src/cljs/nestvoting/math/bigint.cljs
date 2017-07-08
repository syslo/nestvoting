(ns nestvoting.math.bigint
  (:require nestvoting.dependencies.BigInteger))

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

(defn- aupdate [array index fun]
  (aset array index (fun (aget array index)))
  array)

(defn random-bytes [n & {:keys [first-bit last-bit]}]
  (-> (new js/Uint8Array n)
      (doto (->> (.getRandomValues js/crypto)))
      (cond->
        (some? first-bit) (aupdate 0 #(clojure.core/+ (mod % 128) (if first-bit 128 0)))
        (some? last-bit) (aupdate (clojure.core/- n 1) #(clojure.core/- % (mod % 2) (if last-bit -1 0))))
      (->> (.from js/Array))
      (as-> $ (.fromArray js/bigInt $ 256))))
