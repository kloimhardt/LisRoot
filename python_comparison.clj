(native-header "ROOT.h")
(require '[cxx])

(defn Linear []
  (fn [[x] [par0 par1]]
    (+ par0 (* x par1))))

(def c (cxx_> new TCanvas))

(doto ((cxx_> new TF1) "pyf3" (Linear) -1. 1. 2)
  ((cxx_> SetParameters TF1) 5. 2.)
  ((cxx_> Draw TF1)))

((cxx_> Print TCanvas) c "python_comparison_1.pdf")

;; Example 2

(defn LinearB [k d]
  (fn [[x]]
    (+ d (* k x))))

(println ((LinearB 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearB 2.0 4.0) (list 1.0))) ;;comment => 6.0

((cxx_> Draw TF1)
 ((cxx_> new TF1) "pyf2" (LinearB 2. 5.) -1. 1. 2))

((cxx_> Print TCanvas) c "python_comparison_2.pdf")

