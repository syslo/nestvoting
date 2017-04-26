(ns nestvoting.math.hashing
  (:require [pandect.algo.sha256 :refer [sha256]]))


(defmulti secure-hash (fn [x & _] x))

(defmethod secure-hash :sha2-256 [_ x] (sha256 x))
