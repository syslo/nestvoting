(ns nestvoting.math.signature
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.core.async :as async :refer [<!]]))


(defn generate-rsa-key-pair []
  (let [result (async/chan)
        spec (clj->js {:name "RSASSA-PKCS1-v1_5"
                       :modulusLength 2048
                       :publicExponent (new js/Uint8Array (clj->js [0x01, 0x00, 0x01]))
                       :hash {:name "SHA-256"}})]
    (-> js/crypto .-subtle
        (.generateKey spec true (clj->js ["sign" "verify"]))
        (doto (.then #(async/onto-chan result (repeat {:private (.-privateKey %) :public (.-publicKey %)})))))
    result))


(defn- ab->b64 [ab]
  (-> ab
      (->> (new js/Uint8Array)
           (.from js/Array))
      (.map #(.fromCharCode js/String %))
      (.join "")
      (js/btoa)))

(defn- b64->ab [b64]
  (let [raw-string (js/atob b64)
        n (.-length raw-string)
        array (new js/Uint8Array n)]
      (dotimes [i n] (aset array i (.charCodeAt raw-string i)))
      (.-buffer array)))


(defn- s->ab [s cb]
  (let [reader (new js/FileReader)]
    (set! (.-onload reader) #(cb (.-result reader)))
    (.readAsArrayBuffer reader (new js/Blob (clj->js [s])))))


(defn sign [private-key message]
  (let [result (async/chan)]
    (s->ab message
      (fn [raw-message]
        (-> js/crypto .-subtle
            (.sign (clj->js {:name "RSASSA-PKCS1-v1_5"}) private-key raw-message)
            (doto (.then #(do (async/onto-chan result (repeat (ab->b64 %)))))))))
    result))

(defn verify [public-key message signature]
  (let [result (async/chan)]
    (s->ab message
      (fn [raw-message]
        (-> js/crypto .-subtle
            (.verify (clj->js {:name "RSASSA-PKCS1-v1_5"}) public-key (b64->ab signature) raw-message)
            (doto (.then #(async/onto-chan result (repeat %)))))))
    result))


(defn export-public-rsa [public-key]
  (let [result (async/chan)]
    (-> js/crypto .-subtle
        (.exportKey "jwk" public-key)
        (doto (.then #(async/onto-chan result (repeat (js->clj %))))))
    result))

(defn import-public-rsa [data]
  (let [result (async/chan)
        spec (clj->js {:name "RSASSA-PKCS1-v1_5"
                       :hash {:name "SHA-256"}})]
    (-> js/crypto .-subtle
        (.importKey "jwk" (clj->js data) spec true (clj->js ["verify"]))
        (doto (.then #(async/onto-chan result (repeat (js->clj %))))))
    result))
