(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])

(c/add-types [:Types
            [:Classes
             [TCanvas
              [:A string string int int int int]
              [:B int]
              [Print
               [:A null string]
               [:B null int]]
              [TCanvasMethod2
               [:A int]
               [:B string]]]
             [TF1
              [:A string string int int]
              [:B string :plot-function double double double]
              [Draw
               [:A null]]]]
            [:Functions
             [:plot-function double double[1] double[1]]]])

(def pc1 ((c/c-new TCanvas) "c1" "Something" 0 0 800 600))
(def pf1 ((c/c-new TF1) "f1" "sin(x)" -5 5))
((c/c-call TF1 Draw) pf1)
((c/c-call TCanvas Print) pc1 "c_interop_1.pdf")
