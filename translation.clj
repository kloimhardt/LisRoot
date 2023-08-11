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

(def newTF1 (ROO/T new TF1))
(def SetParameters (ROO/T SetParameters TF1))
(def Draw (ROO/T Draw TF1))

(def g (newTF1 "pyf2" l -1. 1. 2))
(SetParameters g 5. 2.)
(Draw g)

(doto (newTF1 "pyf2" l -1. 1. 2)
  (SetParameters 5. 2.)
  Draw)

((cxx__ Print TCanvas) c "translation.pdf")

