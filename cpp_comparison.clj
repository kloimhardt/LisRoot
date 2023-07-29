(native-header "ROOT.h")
(require '[cxx :as c])
(defmacro => [& args] (interop args))

(def pi 3.1415)

(defn single [[x] [r]]
  (pow (/ (sin (* pi r x))
          (* pi r x))
       2))

(defn nslit0 [[x] [r ns]]
  (pow (/ (sin (* pi ns x))
          (sin (* pi x)))
       2))

(defn nslit []
  (fn [x par] (* (single x par) (nslit0 x par))))

(def Fnslit ((=> new TF1) "Fnslit" (nslit) -5.001 5. 2))

((=> SetNpx TF1 [int]) Fnslit 500)

((=> SetParameters TF1) Fnslit 0.2 2)

(def c (cxx__doto (new TCanvas)
                  (Print "empty.pdf")))

((=> Draw TF1) Fnslit)
((=> Print TCanvas) c "nslit.pdf")

;; Example 2

(defn fmul [f g]
  (fn [& params]
    (* (apply f params)
       (apply g params))))

(def Fnslits ((=> new TF1) "Fnslits" (fmul single nslit0) -5.001 5. 2))

((=> SetNpx TF1) Fnslits 500)
((=> SetParameters TF1 :double_int) Fnslits 0.2 2)

((=> Draw TF1) Fnslits)
((=> Print TCanvas) c "nslits.pdf")

;; Benchmarks

(c/m-add-type [:TF1 :Eval] [:default :double [:= :double]])
(def now1 (micros))
(def erg ((c/call TF1 Eval) Fnslits 0.4))
(println "Call once: " erg (- (micros) now1))

(c/m-add-type [:TF1 :GetX]
              [:default :double :double :double :double :int [:= :double]])
(def now (micros))
(def erg ((c/call TF1 GetX)
          Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Calctime: " erg (- (micros) now))

;; Basetime:  40 +-10 (760 +- 100 with Malli runtime check: )
;; Calctime:  12,800 +-300 (14,100 +- 300 with Malli check)
