(native-header "TCanvas.h")
(native-header "TF1.h")
(require '[c_interop :as c])

(c/load-types "root_types.edn")

(def f ((c/new TF1) "pyf2"
        (fn [[x] [par0 par1]]
          (+ par0 (* x par1)))
        -1. 1. 2))

((c/call TF1 SetParameters) f 5. 2.)

(def c ((c/new TCanvas)))

((c/call TF1 Draw) f)
((c/call TCanvas Print) c "python_comparison.pdf")
