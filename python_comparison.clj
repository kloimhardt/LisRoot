(native-header "ROOT.h")
(require '[cxx :as ROO])

;; Example 1

(defn Linear []
  (fn [[x] [d k]]
    (+ d (* x k))))

(def c (ROO/T new TCanvas))

(def f ((ROO/T new TF1) "pyf2" (Linear) -1. 1. 2))
((ROO/T SetParameters TF1) f 5. 2.)
((ROO/T Draw TF1) f)

(def draw (ROO/T Draw TF1)) (draw f)

((cxx__ Print TCanvas) c "python_comparison_1.pdf")

;; Example 1b

(def g (ROO/To
         ((new TF1) "pyf2" (Linear) -1. 1. 2)
         (SetParameters 5. 2.)
         Draw))

((cxx__ Print TCanvas) c "python_comparison_1b.pdf")

;; Example 1c

(ROO/Ts [:TF1 :SetParameters :line]
        [[:d ::pos-int] [:k ::pos]]
        [:double :double])

(def params {:d 10 :k -2})

(ROO/To ((bless TF1) g)
        ((SetParameters :line) params)
        Draw)

((cxx__ Print TCanvas) c "python_comparison_1c.pdf")

;; Example 1d
(defn LinearA [[x] [d k]]
  (+ d (* x k)))

(ROO/Ts-default [:TF1 :SetParameters :line])

(ROO/To ((new TF1 :XR2) LinearA -1. 1.)
        (SetParameters {:d 10 :k 2})
        Draw)

((cxx__ Print TCanvas) c "python_comparison_1d.pdf")

;; Example 2

(defn LinearB [k d]
  (fn [[x]]
    (+ d (* k x))))

((ROO/T Draw TF1)
 ((ROO/T new TF1 :XR2) (LinearB 2. 5.) -1. 1.))

(ROO/To ((bless TCanvas) c)
        (Print "python_comparison_2.pdf"))

;; native

(native-declare "
double linear(double* arr, double* par) {
  return par[0] + arr[0]*par[1];
} ")

(ROO/To ((new TF1 :XR2-native linear) -1. 1.)
        (SetParameters {:d 15 :k 2})
        Draw)

((cxx__ Print TCanvas) c "python_comparison_native.pdf")

;; Calculation

(println ((LinearB 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearB 2.0 4.0) (list 1.0))) ;;comment => 6.0


