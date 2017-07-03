(ns nestvoting.math.group
  (:require [nestvoting.math.bigint :as N]
            [nestvoting.math.algorithm :as algo]
            [nestvoting.config :refer [in-config]]))

(defprotocol Group
  (e [G])
  (* [G x y])
  (inv [G x])
  (= [G x y]))

(defprotocol CyclicGroup
  (g [G])
  (order [G]))

(defprotocol SerializableGroup
  (serialize [G])
  (serialize-elem [G x])
  (deserialize-elem [G s]))

(defprotocol RandomizableGroup
  (randomize [G]))

(defmulti deserialize (comp keyword :type))

(defn div [G x y]
  (* G x (inv G y)))

(defn pow [G x n]
  (algo/associative-pow #(* G %1 %2) (e G) x n))

(defn *s [G xs]
  (reduce #(* G %1 %2) (e G) xs))

;;
;; Schnorr group
;;

(def schnorr-bigint-params [:p :q :g])

(defn schonrr-construct [{:keys [p q g] :as params}]
  (reify
    Group
    (e [_] N/_1)
    (* [_ x y] (algo/mod-* x y p))
    (inv [G x] (pow G x (N/- (order G) N/_1)))
    (= [_ x y] (N/= x y))
    CyclicGroup
    (g [_] g)
    (order [_] q)
    SerializableGroup
    (serialize [_] (reduce #(update %1 %2 N/str) params schnorr-bigint-params))
    (serialize-elem [_ x] (N/str x))
    (deserialize-elem [_ s] (N/val s))
    RandomizableGroup
    (randomize [G] (schonrr-construct
                     (update params :g
                             #(pow G % (algo/random N/_1 q)))))))

(defmethod deserialize :schnorr [params]
  (schonrr-construct (reduce #(update %1 %2 N/val) params schnorr-bigint-params)))

(defn schnorr-generate [p-size q-size]
  (let [q (->> (N/random-bytes (quot (+ q-size 7) 8) :first-bit true :last-bit true)
               (iterate #(N/+ % N/_2))
               (filter algo/miller-rabin)
               (first))
        [p r] (->> (N/random-bytes (quot (- p-size q-size -7) 8) :first-bit false :last-bit false)
                   (iterate #(N/+ % N/_2))
                   (map (fn [r] [(N/+ (N/* q r) N/_1) r]))
                   (filter (comp algo/miller-rabin first))
                   (first))
        G (->> (repeatedly #(N/random-bytes (quot (+ q-size 7) 8)))
               (map #(algo/mod-pow % r p))
               (filter #(not (N/= % N/_1)))
               (map #(schonrr-construct {:p p :q q :g % :type :schnorr}))
               (first))]
    G))


;;
;; Loading
;;

(defn effective-random-group []
  (randomize (deserialize (in-config :math :group))))
