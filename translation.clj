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

(def f (newTF1 "pyf2" l -1. 1. 2))
(SetParameters f 5. 2.)
(Draw f)

(doto (newTF1 "pyf2" l -1. 1. 2)
  (SetParameters 5. 2.)
  Draw)

((cxx__ Print TCanvas) c "translation_1.pdf")

(ROO/Ts [:TF1 :Draw :my-hint]
        [:string])

((ROO/T Draw TF1 :my-hint) f "P")

((cxx__ Print TCanvas) c "translation_2.pdf")

(ROO/Ts [:TF1 :Draw :your-hint]
        [:string]
        [[:style ::one-letter]])

(defn fallbackDraw [f params]
  (when (:mismatch
         ((ROO/T Draw TF1 :your-hint) f params))
    ((ROO/T Draw TF1) f)))

(fallbackDraw f {:style "P"})

((cxx__ Print TCanvas) c "translation_3.pdf")

(fallbackDraw f {:style "unknown"})

((cxx__ Print TCanvas) c "translation_4.pdf")

(native-declare "typedef std::string StdStr;")
(def s1 ((ROO/T new StdStr) "Hello "))
(def s2 ((ROO/T append StdStr) s1 "World"))
(println s2 "!")
