(require '[malli.core :as m])

(def =>data
  (m/schema (read-string (slurp "malli1.edn"))))

(def plot-function [:double [3.4 1.2] [5.6 7.8]])

(def data {:TCanvas {:default []
                     :B ["c", "Something", 0, 0, 800, 600]
                     :Print {:default [:nil "file.pdf"]}}
           :TF1 {:default ["FunctionName" plot-function -5.001 5. 2.0]
                 :native ["FunctionName" "placeholder foo" -5.001 5. 2.0]
                 :Draw {:default [:nil]}
                 :SetParameters {:default [:nil 0.2 2.0]
                                 :double_int [:nil 0.2 2]}}})

(println (m/validate =>data data))
(println (m/explain =>data data))

