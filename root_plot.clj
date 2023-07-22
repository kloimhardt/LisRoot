(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/m-load-types "malli_types.edn")
(defmacro => [& args] (bake-safe args))

(defn n-times-x [n]
  (fn [[x]] (* n x)))

(def pf1 ((=> new TF1) "f1" (n-times-x 2) -5.001 5.0 2))
(def pc1 ((=> new TCanvas :B) "c1" "Something" 0 0 800 600))

((=> Draw TF1) pf1)
((=> Print TCanvas) pc1 "root_plot_1.pdf")

(def pf2 ((=> new TF1 [:B string string int int]) "f2" "sin(x)" -5 5))
(def pc2 ((=> new TCanvas :B) "c2" "Something" 0 0 800 600))

((=> Draw TF1) pf2)
((=> Print TCanvas) pc2 "root_plot_2.pdf")
