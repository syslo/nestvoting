(ns nestvoting.math.signature
  (:require [clojure.core.async :as async]
            [clojure.string :as str]
            [cheshire.core :as json])
  (:import (java.security KeyPairGenerator Signature KeyFactory)
           (java.nio.charset StandardCharsets)
           (java.util Base64 Arrays)
           (java.security.interfaces RSAPublicKey RSAPrivateKey)
           (java.security.spec RSAPublicKeySpec)
           (com.nimbusds.jose.jwk RSAKey)))


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

(defn export-public-rsa [^RSAPublicKey public-key]
  (-> (new RSAKey public-key nil nil nil nil nil nil nil)
      (.toJSONString)
      (json/parse-string)
      (repeat)
      (async/to-chan)))

(defn import-public-rsa [data]
  (-> (json/generate-string data)
      (RSAKey/parse)
      (.toRSAPublicKey)
      (repeat)
      (async/to-chan)))

(defn export-rsa-pair [{:keys [^RSAPublicKey public ^RSAPrivateKey private]}]
  (-> (new RSAKey public private nil nil nil nil nil nil nil)
      (.toJSONString)
      (json/parse-string)
      (repeat)
      (async/to-chan)))

(defn import-rsa-pair [data]
  (-> (json/generate-string data)
      (RSAKey/parse)
      (as-> $ {:public (.toRSAPublicKey $) :private (.toRSAPrivateKey $)})
      (repeat)
      (async/to-chan)))
