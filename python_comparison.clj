(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/load-types "root_types.edn")
(c/m-load-types "malli1.edn")

(defn Linear []
  (fn [[x] [par0 par1]]
    (+ par0 (* x par1))))

(def f ((c/new TF1) "pyf2" (Linear) -1. 1. 2))

((c/call TF1 SetParameters) f 5. 2.)

(def c (c/new TCanvas))

((c/call TF1 Draw) f)
((c/call TCanvas Print) c "pyf2.pdf")


;; Example 2

(defn LinearB [k d]
  (fn [[x]]
    (+ d (* k x))))

(println ((LinearB 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearB 2.0 4.0) (list 1.0))) ;;comment => 6.0

((c/call TF1 Draw)
 ((c/new TF1) "pyf3" (LinearB 2. 5.) -1. 1. 2))

((c/call TCanvas Print) c "pyf3.pdf")

