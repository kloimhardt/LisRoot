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

((cxx__ Print TCanvas) c "python_comparison_1a.pdf")

;; Example 1b

(def g (ROO/To
         ((new TF1) "pyf2" l -1. 1. 2)
         (SetParameters 5. 2.)
         Draw))

((cxx__ Print TCanvas) c "python_comparison_1b.pdf")

;; draw

((ROO/T Draw TF1 :plot-style) g "P")

((cxx__ Print TCanvas) c "python_comparison_2a.pdf")

(ROO/Ts [:TF1 :Draw :named]
        [:string]
        [[:style :string]])

((ROO/T Draw TF1 :named) g {:style "P"})

  (defn Draw [& [[class _ :as obj] style]]
    (cond
      (and (= class "TF1") (= style "dotted"))
      ((ROO/T Draw TF1 :plot-style) obj "P")
      :else
      ((ROO/T Draw TF1) obj)))

(comment
  (Draw g "dotted")
;;
  )

((cxx__ Print TCanvas) c "python_comparison_2b.pdf")

(Draw g)
((cxx__ Print TCanvas) c "python_comparison_2c.pdf")

;; add type

(ROO/Ts [:TF1 :SetParameters :line]
        [:double :double]
        [[:d ::pos-int] [:k ::pos]])

(def params {:d 10 :k -2})

(doto g
  ((ROO/T SetParameters TF1 :line) params)
  (Draw "dotted"))

((cxx__ Print TCanvas) c "python_comparison_3.pdf")

;; native

(native-declare "
  double linear(double* arr, double* par) {
    return par[0] + arr[0]*par[1];
  } ")

(ROO/Ts-default [:TF1 :SetParameters :line])

(ROO/To ((new TF1 :XR2-native linear) -1. 1.)
        (SetParameters {:d 10 :k 2})
        ((Draw :plot-style) "P"))

((cxx__ Print TCanvas) c "python_comparison_4.pdf")

;; functional

(defn LinearA [d k]
  (fn [[x]]
    (+ d (* k x))))

(doto ((ROO/T new TF1 :XR2) (LinearA 5 2) -1. 1.) (Draw "dotted"))

(ROO/To ((bless TCanvas) c)
        (Print "python_comparison_5.pdf"))

;; simple function

(defn LinearB [[x] [d k]]
  (+ d (* x k)))

(ROO/To ((new TF1 :XR2) LinearB -1. 1.)
        (SetParameters {:d 10 :k 2})
        Draw)

((cxx__ Print TCanvas) c "python_comparison_6.pdf")

;; Calculation

(println ((LinearA 2.0 5.0) (list 1.0))) ;;comment => 7.0
(println ((LinearA 2.0 4.0) (list 1.0))) ;;comment => 6.0


(comment

  (defn SetParameters [& args]
    (let [strclass (first (first args))]
      (when (= strclass "TF1")
        (cond
          (= (count args) 2)
          (apply (ROO/T SetParameters TF1 :linear) args)
          :else
          (apply (ROO/T SetParameters TF1) args)))))

  #_end)
