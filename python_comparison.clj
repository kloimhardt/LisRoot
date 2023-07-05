(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/load-types "root_types.edn")

(defn l []
  (fn [[x] [par0 par1]]
    (+ par0 (* x par1))))

(def f ((c/new TF1) "pyf2" (l) -1. 1. 2))

((c/call TF1 SetParameters) f 5. 2.)

(def c ((c/new TCanvas)))

((c/call TF1 Draw) f)
((c/call TCanvas Print) c "pyf2.pdf")


;; Example 2

(defn linear [par0 par1]
  (fn [[x]]
    (+ par0 (* x par1))))

((c/call TF1 Draw)
 ((c/new TF1) "pyf3" (linear 5. 2.) -1. 1. 2))

((c/call TCanvas Print) c "pyf3.pdf")
