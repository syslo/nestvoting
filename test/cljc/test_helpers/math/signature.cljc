(ns test-helpers.math.signature
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require #?(:clj  [clojure.core.async :refer [<! go]])
            #?(:cljs [cljs.core.async :refer [<!]])
            [nestvoting.math.signature :as signature]))

(def signature-examples
  [{:msg "This is a test message"
    :sgn "czgPxNtDTg3P8gpJp4y3IEp8Yf8NWo7jY/J+Ql6NY4PEDokj+GwsfyYbX1OdERQIDeRPSMBuioBryekIvVsuePOQrr2u4QM/7kjRDLt+aitVplioqJ7jb+o1Kq06hpeKbXtlOq5LNbSzAcHvNQTEqOEVrHT1lCM8u4kSlLo56uMvhYal1c7YaMAPu24X6xstCuGphTjsSzz6iEvKAEKyKwz0NLYGYzZsQmtd5T8Edd35UTyPD8lUYSFMqNTHr3V3YGep6RDYSQ/HtPxI+JjA4SKW8JRDAyEl7Yf5BD2MFVUJCVymrQkuXK07tzVZV5ZmgmRlKgw1NMPlb4kZ8AerPg=="
    :public-key {:kty "RSA"
                 :e "AQAB",
                 :n "of1arapFNUdccdBmcLqBDm41U9nZL-gCP5fdwHyIlg088kiqRMvx1VX2D3zTJg92ao2HhtoHDclpVU2-N8HDSW7kEQydbvhqvG1cc5V70I09yyWW4_oAFlkul2Pn8Sw-KKO-Oj821KRFmb_E3NGl3UcI25GHp44yHsVAieMtEVhE-Vav6_kwStOt-L4E1w2_zTX91fyTJRjr71nHI1t3B1cEDAgjCDDw3XHUJMLFwL3HU6VHH8VVCc4Mq2Gm9wSvhPHlpNiW0SLkxrt-Q9Hvv1ITd-cEds8Wbfe437pULIaLWI92Gp7W8lsZ0SOpf2QhDmL7J_buwPuZvgB0v36MKw"}}
   {:msg "This is a test message"
    :sgn "PYqi8acGZgXtyzK527I+d3oo7pKtIS9Jll2FLbRAMG3BbX2dByzTlmIqULnH7zmCFJtTf4JyUJN1+eruPgavj+R4XKrrb+TmzZmaRcr9KWWArsZ74+ra56h6tPDPU/xMMYPHN4bNrtWHPgChdpZDSXpzIsXtpdUMMiZU02qa8/8s9ToWnjZuqEaLgZsXK4CE/UFjzjTmKN1DRT2kYCz97gdeHwt9GY88YcQ8QeNBd3nKDtRtvkT1NrTd6YU/FPWs3/31KnWUB90P9liwyVFN5CyFKlGnlLZAfKCtIpUZVAIdvtsfiWKkyQ4QdNKUaURdIsYfvfrgJvS8k15aymLeNA=="
    :public-key {:kty "RSA",
                 :e "AQAB"
                 :n "tjanJxOp_uEkXYN35rFHesXfKOr723f4IjfAd5GqhQkaWDsjw8gt31wZCgEy97J892xSKSFOTdi1s9kokCmfJyaVkNjyZeVVel4usibeh8Kcb6_V07OpslnNc-RDSzlPFh4F0a7dP5pBAqJT1DwK0GpoVG6iX2wYci-2IV4wITZeGoBV7O53eeOsqrrCaG9ysSiroFfiBjdTMOnEQnoAqiv1AryhKepgnVdSELl1kJBpNDO6SbI5a8FOGCecZnAuASO8O0Wtml5pCpGYRdhZeXpI_UZ2E-TVtrbHyVQyN-ZCFuwHMAzGM7BQ7mDvSEgQPuZPIHytpIUfh-fY3F2UPw"}}])

(defn construct-signature-example [msg]
  (go
    (let [{:keys [public private]} (<! (signature/generate-rsa-key-pair))
          sgn (<! (signature/sign private msg))
          public-key (<! (signature/export-public-rsa public))]
      {:msg msg
       :sgn sgn
       :public-key public-key})))

(defn validate-signature-example [{:keys [msg sgn public-key]}]
  (go
    (let [public (<! (signature/import-public-rsa public-key))]
      (<! (signature/verify public msg sgn)))))
