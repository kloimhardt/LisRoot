(native-header "ROOT.h")
(require '[cxx :as ROO])

(defn Linear []
  (fn [[x] [d k]]
    (+ d (* x k))))

(def l (Linear))

(def c (ROO/T new TCanvas))

(def f ((ROO/T new TF1) "pyf2" l -1. 1. 2))
((ROO/T SetParameters TF1) f 5. 2.)
((ROO/T Draw TF1) f)

(def newTF1 (ROO/T new TF1))
(def SetParameters (ROO/T SetParameters TF1))
(def Draw (ROO/T Draw TF1))

(def g (newTF1 "pyf2" l -1. 1. 2))
(SetParameters g 5. 2.)
(Draw g)

(doto (newTF1 "pyf2" l -1. 1. 2)
  (SetParameters 5. 2.)
  Draw)

((cxx__ Print TCanvas) c "translation_1.pdf")

((ROO/T Draw TF1) f)

(def simple-draw (ROO/T Draw TF1))
(simple-draw f)

(ROO/Ts [:TF1 :Draw :my-hint]
        [:string])

(def option-draw (ROO/T Draw TF1 :my-option))
(option-draw f "P")

(ROO/Ts [:TF1 :Draw :your-hint]
        [:string]
        [[:style ::one-letter]])

(defn fallback-draw [h params]
  (when (:mismatch ((ROO/T Draw TF1 :your-option) h params))
    ((ROO/T Draw TF1) h)))

(fallback-draw f {:style "P"})

(fallback-draw f {:style "unknown"})

((cxx__ Print TCanvas) c "translation_2.pdf")
