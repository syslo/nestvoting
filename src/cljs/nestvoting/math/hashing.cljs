(ns nestvoting.math.hashing
  (:require nestvoting.dependencies.sha256))


(defmulti secure-hash (fn [x & _] x))

(defmethod secure-hash :sha2-256 [_ x]
  (-> (new js/jsSHA "SHA-256" "TEXT")
      (doto (.update x))
      (.getHash "HEX")))
