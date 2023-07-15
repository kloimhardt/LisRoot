(native-header "TCanvas.h")
(native-header "TF1.h")
(require '[c_interop :as c])
(c/load-types "root_types.edn")

(defmacro TF1 [& args] (bake args "TF1"))
(defmacro TCanvas [& args] (bake args "TCanvas"))

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

(def Fnslit ((TF1) "Fnslit" (nslit) -5.001 5. 2))

(c/add-signature [TF1 SetNpx] [:A null int])

((TF1 SetNpx) Fnslit 500)

((TF1 SetParameters) Fnslit 0.2 2)

(def c (TCanvas))

((TF1 Draw) Fnslit)
((TCanvas Print) c "nslit.pdf")

;; Example 2

(defn fmul [f g]
  (fn [& params]
    (* (apply f params)
       (apply g params))))

(def Fnslits ((c/new TF1) "Fnslits" (fmul single nslit0) -5.001 5. 2))

((c/call TF1 SetNpx) Fnslits 500)
((c/call TF1 SetParameters) Fnslits 0.2 2)

((c/call TF1 Draw) Fnslits)
((c/call TCanvas Print) c "nslits.pdf")

(c/add-signature [TF1 Eval] [:A double double])

(def now1 (micros))
(def erg ((c/call TF1 Eval) Fnslits 0.4))
(println "Basetime: " erg (- (micros) now1))

(c/add-signature [TF1 GetX]
                 [:A double double double double double int])

(def now (micros))
(def erg ((c/call TF1 GetX) Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Calctime: " erg (- (micros) now))

;; Basetime:  40 +-10
;; Calctime:  12,800 +-300
