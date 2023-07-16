(native-header "TCanvas.h")
(native-header "TF1.h")

(require 'c_interop)
(defmacro => [& args] ((with-types "root_types.edn") args))

(defmacro g [& args] ((with-types-check "root_types.edn") args))

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

((=> SetNpx TF1 [:A int]) Fnslit 500)

((=> SetParameters TF1) Fnslit 0.2 2)
((g SetParameters TF1) Fnslit 0.2 2)

(println "uu" uu)

(def c (=> new TCanvas))

((=> Draw TF1) Fnslit)
((=> Print TCanvas) c "nslit.pdf")

;; Example 2

(defn fmul [f g]
  (fn [& params]
    (* (apply f params)
       (apply g params))))

(def Fnslits ((=> new TF1) "Fnslits" (fmul single nslit0) -5.001 5. 2))

((=> SetNpx TF1) Fnslits 500)
((=> SetParameters TF1) Fnslits 0.2 2)

((=> Draw TF1) Fnslits)
((=> Print TCanvas) c "nslits.pdf")

(def now1 (micros))
(def erg ((=> Eval TF1 [:A double -> double]) Fnslits 0.4))
(println "Basetime: " erg (- (micros) now1))

(def now (micros))
(def erg ((=> GetX TF1 [:A double double double double int -> double])
          Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Calctime: " erg (- (micros) now))

;; Basetime:  40 +-10
;; Calctime:  12,800 +-300
