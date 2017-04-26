(ns nestvoting.math.hashing)


(defmulti secure-hash (fn [x & _] x))

(defmethod secure-hash :sha2-256 [_ x]
  (-> (new js/jsSHA "SHA-256" "TEXT")
      (.update x)
      (.getHash "HEX")))
