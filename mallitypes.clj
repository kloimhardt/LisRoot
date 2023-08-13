(require '[malli.core :as m])

(def =>data
  (m/schema (read-string (slurp "malli_types.edn"))))

(def plot-function [[3.4 1.2] [5.6 7.8] :double])
(def R1R2->R [[3.4] [5.6 7.8] :double])

(def data {:TCanvas {:minimal []
                     :B ["c", "Something", 0, 0, 800, 600]
                     :Print {:default ["file.pdf"]}}
           :TF1 {:XRN ["FunctionName" plot-function -5.001 5. 2]
                 :XR2 ["\"XR2\"" R1R2->R -5.001 5. 2]
                 :XR2-native ["\"XR2\"" 1 -5.001 5. 2]
                 :Draw {:no-args [] :plot-option ["P"]}
                 :SetParameters {:two-doubles [0.2 2.0]
                                 :linear {:rtm {:d 5 :k 2.0}
                                          :cxx [5.0 2.0]}}}})

(println (m/validate =>data data))
(println (m/explain =>data data))

