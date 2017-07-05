(ns nestvoting.math.signature
  (:require [clojure.core.async :as async]
            [clojure.string :as str])
  (:import (java.security KeyPairGenerator Signature KeyFactory)
           (java.nio.charset StandardCharsets)
           (java.util Base64 Arrays)
           (java.security.interfaces RSAPublicKey)
           (java.security.spec RSAPublicKeySpec)))


(defn generate-rsa-key-pair []
  (-> (KeyPairGenerator/getInstance "RSA")
      (doto (.initialize 2048))
      (.generateKeyPair)
      (as-> $ {:private (.getPrivate $) :public (.getPublic $)})
      (repeat)
      (async/to-chan)))


(defn sign [private-key message]
  (-> (Signature/getInstance (str "SHA256with" (.getAlgorithm private-key)))
      (doto (.initSign private-key)
            (.update (.getBytes message StandardCharsets/UTF_8)))
      (.sign)
      (->> (.encode (Base64/getEncoder))
           (new String))
      (repeat)
      (async/to-chan)))

(defn verify [public-key message signature]
  (-> (Signature/getInstance (str "SHA256with" (.getAlgorithm public-key)))
      (doto (.initVerify public-key)
            (.update (.getBytes message StandardCharsets/UTF_8)))
      (.verify (.decode (Base64/getDecoder) (.getBytes signature)))
      (repeat)
      (async/to-chan)))

(defn- bi->b64u [bi]
  (-> bi
      .toByteArray
      (as-> $ (loop [i 0] (if (zero? (aget $ i))
                            (recur (inc i))
                            (Arrays/copyOfRange $ i (count $)))))
      (->> (.encode (Base64/getEncoder))
           (new String))
      (str/replace "+" "-")
      (str/replace "/" "_")
      (str/replace #"[=]*$" "")))

(defn- b64u->bi [b64u]
  (-> b64u
      (str/replace "-" "+")
      (str/replace "_" "/")
      (.getBytes)
      (->> (.decode (Base64/getDecoder))
           (new BigInteger 1))))


(defn export-public-rsa [^RSAPublicKey public-key]
  (-> {:kty "RSA"
       :e (-> public-key .getPublicExponent bi->b64u)
       :n (-> public-key .getModulus bi->b64u)}
      (repeat)
      (async/to-chan)))

(defn import-public-rsa [data]
  (let [spec (new RSAPublicKeySpec (-> data :n b64u->bi)
                                   (-> data :e b64u->bi))]
    (-> (KeyFactory/getInstance "RSA")
        (.generatePublic spec)
        (repeat)
        (async/to-chan))))
