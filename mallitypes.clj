(require '[malli.core :as m])

(def =>data
  (m/schema (read-string (slurp "malli_types.edn"))))

(def plot-function [[3.4 1.2] [5.6 7.8] :double])
(def R1R2->R [[3.4] [5.6 7.8] :double])

(def data {:TCanvas {:default []
                     :B ["c", "Something", 0, 0, 800, 600]
                     :Print {:default ["file.pdf"]}}
           :TF1 {:default ["FunctionName" plot-function -5.001 5. 2]
                 :XR2 ["FunctionName" R1R2->R -5.001 5. 2]
                 :XR2-native ["FunctionName" 1 -5.001 5. 2]
                 :Draw {:default []}
                 :SetParameters {:default [0.2 2.0]
                                 :double_int [0.2 2]}}})

(println (m/validate =>data data))
(println (m/explain =>data data))

