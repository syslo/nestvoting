(ns tests.math.groups
  (:require [nestvoting.math.bigint :as N]
            [nestvoting.math.group :as G]
            [nestvoting.math.algorithm :as algo])
  #?(:clj (:use clojure.test))
  #?(:cljs (:require-macros [cljs.test :refer [deftest is]])))

(defn generic-group-test [G get-elem]
  (let [[x1 x2 x3] (repeatedly 3 get-elem)]
    (is (G/= G (G/e G) (G/* G (G/e G) (G/e G))))
    (is (G/= G x1 (G/* G (G/e G) x1)))
    (is (G/= G (G/e G) (G/* G x2 (G/inv G x2))))
    (is (G/= G (G/e G) (G/div G x3 x3)))
    (is (G/= G (G/* G (G/* G x1 x2) x3) (G/* G x1 (G/* G x2 x3))))))


(defn random-cyclic-group-elem [G]
  (G/pow G (G/g G) (algo/random N/_1 (N/- (G/order G) N/_1))))

(defn cyclic-group-test [G n]
  (doall (repeatedly n
           (fn [] (generic-group-test G #(random-cyclic-group-elem G)))))
  (is (G/= G (G/pow G (G/g G) (G/order G)) (G/e G))))

(defn generate-effective-random-group []
  (G/randomize
    (G/deserialize
      {:p "14789938546373007430446515753873426540030936302845350163990337427619342559568562958908471796124398925007176954281365291244540703316758288600611622362383340591932546423360291837924270181612294972974302808979583112089536785763567267294820002600755158628364271763108728803826180114598869227311733483417096152198001259598775949561435299968203796348855335960433550902849436005222230618116850217022904439494889930297180527565700227226936371940655442135390722847993718379157314408475573059769738210899326888125950973110313480018442520978734841256899072606077136396623129776253348797702687152559712541820688984957227745019443",
       :q "15376105530855768580302606861556854317166222861165706234443676092811",
       :g "11449894680143820212362539972105177067253774220877845510224302770516288444356877401546428161103470581081020708937937799062258290806612311041648823469711674351748650633503411438698437401287730031203043216910889968388802890216786519625141408096968061072461135963781336973131654777737027204445488677363556812049954694435859892235729985319378207159693302252135797220543922124356697125513135648610939332513387724555683289560910384266787074724382864676512118448531096876768266585211416511594488279132164069034743928182804560016322349079356190989062100268721875560391623509761008225248674597342703738760150568754919748368052",
       :type :schnorr})))

(deftest efective-random-group []
  (let [G (generate-effective-random-group)]
    (cyclic-group-test G 5)))

(deftest small-schnorr-group []
  (doall
    (repeatedly 10
      #(cyclic-group-test (G/schnorr-generate 47 42) 20))))