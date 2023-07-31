(native-header "ROOT.h")
(require '[cxx :as ROO])

(defn Linear [[x] [d k]]
  (+ d (* x k)))

(ROO/Ts [:TF1 :SetParameters :line]
        [[:d ::pos-int] [:k ::pos]]
        [:double :double])

(def c (ROO/T new TCanvas))

(ROO/To ((new TF1 :XR2) "pyf2" Linear -1. 1.)
        ((SetParameters :line) {:d 5 :k 2})
        Draw)

((cxx__ Print TCanvas) c "python_comparison_1.pdf")

;; Example 2

(defn LinearB [k d]
  (fn [[x]]
    (+ d (* k x))))

(println ((LinearB 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearB 2.0 4.0) (list 1.0))) ;;comment => 6.0

((cxx__ Draw TF1)
 ((cxx__ new TF1) "pyf2" (LinearB 2. 5.) -1. 1. 2))

((cxx__ Print TCanvas) c "python_comparison_2.pdf")

(ROO/Ts-default [:TF1 :SetParameters :line])

(cxx_> ((new TF1) "pyf2" (identity Linear) -1. 1. 2)
       (SetParameters {:d 5 :k 2})
       Draw)

(cxx_> ((bless TCanvas) c)
       (Print "python_comparison_3.pdf"))
