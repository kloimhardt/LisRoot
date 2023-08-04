(native-header "ROOT.h")
(require '[cxx :as ROO])

;; Example 1

(defn Linear []
  (fn [[x] [d k]]
    (+ d (* x k))))

(def l (Linear))

(def c (ROO/T new TCanvas))

(def f ((ROO/T new TF1) "pyf2" l -1. 1. 2))
((ROO/T SetParameters TF1) f 5. 2.)
((ROO/T Draw TF1) f)

(def draw (ROO/T Draw TF1)) (draw f)

((cxx__ Print TCanvas) c "python_comparison_1.pdf")

;; Example 1b

(def g (ROO/To
         ((new TF1) "pyf2" l -1. 1. 2)
         (SetParameters 5. 2.)
         Draw))

((cxx__ Print TCanvas) c "python_comparison_1b.pdf")

;; native

(native-declare "
double linear(double* arr, double* par) {
  return par[0] + arr[0]*par[1];
} ")

(def h (ROO/To ((new TF1 :XR2-native linear) -1. 1.)
               (SetParameters 5. 2.)
               Draw))

((cxx__ Print TCanvas) c "python_comparison_native.pdf")

;; doto Example

(defn SetParameters [& args]
  (let [strclass (first (first args))]
    (when (= strclass "TF1")
      (cond
        (= (count args) 2)
        (apply (ROO/T SetParameters TF1 :linear) args)
        :else
        (apply (ROO/T SetParameters TF1) args)))))

(defn Draw [& args]
  (let [strclass (first (first args))]
    (when (= strclass "TF1")
      (apply (ROO/T Draw TF1) args))))

(doto h (SetParameters {:d 15 :k -3}) Draw)

(defn Print [& args]
  (let [strclass (first (first args))]
    (when (= strclass "TCanvas")
      (apply (ROO/T Print TCanvas) args))))

(Print c "python_comparison_multi_native.pdf")

(doto g (SetParameters 5. 2.) Draw)

(Print c "python_comparison_multi_lisp.pdf")

;; Example 1c

(ROO/Ts [:TF1 :SetParameters :line]
        [[:d ::pos-int] [:k ::pos]]
        [:double :double])

(def params {:d 10 :k -2})

(ROO/To ((bless TF1) g)
        ((SetParameters :line) params)
        Draw)

(Print c "python_comparison_1c.pdf")

;; Example 1d
(defn LinearA [[x] [d k]]
  (+ d (* x k)))

(ROO/Ts-default [:TF1 :SetParameters :line])

(ROO/To ((new TF1 :XR2) LinearA -1. 1.)
        (SetParameters {:d 10 :k 2})
        Draw)

(Print c "python_comparison_1d.pdf")

;; Example 2

(defn LinearB [k d]
  (fn [[x]]
    (+ d (* k x))))

((ROO/T Draw TF1)
 ((ROO/T new TF1 :XR2) (LinearB 2. 5.) -1. 1.))

(ROO/To ((bless TCanvas) c)
        (Print "python_comparison_2.pdf"))

;; Calculation

(println ((LinearB 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearB 2.0 4.0) (list 1.0))) ;;comment => 6.0


