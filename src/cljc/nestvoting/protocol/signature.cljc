(ns nestvoting.protocol.signature
  (:require #?(:clj  [clojure.core.async :as async])
            #?(:cljs [cljs.core.async :as async])
            [clojure.walk :as walk]
            #?(:clj [cheshire.core :as json])
            [clojure.string :as string]
            [nestvoting.math.signature :as math.signature]
            [nestvoting.math.hashing :as math.hashing]))


(defn- canonical-scalar [value]
  #?(:clj
     (json/generate-string value))
  #?(:cljs
     (.stringify js/JSON (clj->js value))))

(defn canonical-representation [value]
  (cond
    (map? value) (as-> value $
                       (sort-by first $)
                       (map (fn [[k v]] (str (canonical-scalar k) ":" (canonical-representation v))) $)
                       (string/join "," $)
                       (str "{" $ "}"))
    (sequential? value) (as-> value $
                              (map canonical-representation $)
                              (string/join "," $)
                              (str "[" $ "]"))
    :else (canonical-scalar value)))

(defn signable-representation [message]
  (canonical-representation (dissoc message :signature)))

(defn sign-message [private-key message]
  (async/map #(assoc message :signature %)
             [(math.signature/sign private-key (signable-representation message))]))

(defn verify-message [public-key message]
  (math.signature/verify public-key (signable-representation message) (:signature message)))

(defn hash-message [message]
  (math.hashing/secure-hash :sha2-256 (signable-representation message)))
