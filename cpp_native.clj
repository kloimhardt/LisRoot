(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/m-load-types "malli_types.edn")
(defmacro => [& args] (bake-safe args))

(defmacro overload []
  (defn smul [args] (apply str (interpose "*" args)))
  (defn fmul [args] (fn [& params]
                      (smul (map (fn [f] (apply f params))
                                 args))))
  (defn * [& args] (if (fn? (first args))
                     (fmul args)
                     (smul args)))
  (defn sin [arg] (str "sin(" arg ")"))
  (defn pow [x n] (str "pow(" x ", " n ")"))
  (defn / [a b] (str a "/(" b ")"))
  nil)

(overload)

(defmacro nslit [x r ns]
  (def pi 3.1415)

  (defn single [x r ns]
    (pow (/ (sin (* pi r x))
            (* pi r x))
         2))

  (defn nslit0 [x r ns]
    (pow (/ (sin (* pi ns x))
            (sin (* pi x)))
         2))

  ((* single nslit0) x r ns))

(println (nslit "x" 0.2 2))
;;=> pow(sin(3.1415*0.2*x)/(3.1415*0.2*x),2)*pow(sin(3.1415*2*x)/(sin(3.1415*x)),2)

(def c (=> new TCanvas))

(def Fnslits ((=> new TF1 [:B string string int int])
              "Fnslits" (nslit "x" 0.2 2) -5 5))

((=> SetNpx TF1 [:default int]) Fnslits 500)
((=> Draw TF1) Fnslits)
((=> Print TCanvas) c "nslits_native.pdf")

(c/defnative "double cpp_nslit(double* x, double* par)"
  ((* single nslit0) "x[0]" 0.2 2))

(def FastSlits ((=> new TF1 :native cpp_nslit) "Fnslit" "native" -5.001 5. 2))

((=> SetNpx TF1) FastSlits 500)
((=> Draw TF1) FastSlits)
((=> Print TCanvas) c "nslits_fast.pdf")

;; Benchmarks

(c/m-add-type [:TF1 :Eval] [:default :double :-> :double])

(def now1 (micros))
(def erg1 ((c/call TF1 Eval) Fnslits 0.4))
(println "Call once: " erg1 (- (micros) now1))

(c/m-add-type [:TF1 :GetX]
              [:default :double :double :double :double :int :-> :double])

(def now2 (micros))
(def erg2 ((c/call TF1 GetX) Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Root-runtime-compile: " erg2 (- (micros) now2))

(def now3 (micros))
(def erg3 ((c/call TF1 GetX) FastSlits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Native interop: " erg3 (- (micros) now3))

(native-declare
  "double nslitfun(double* x, double* par){
  return pow(sin((3.1415*0.2*x[0]))/(3.1415*0.2*x[0]),2)*pow(sin((3.1415*2*x[0]))/sin((3.1415*x[0])),2);
}

double runit() {
  TF1 *Cslit  = new TF1(\"Fnslit\",nslitfun,-5.001,5.,2);
  Cslit->SetNpx(500);
  return Cslit->GetX(3.6, -5.0, 0.3, 1.E-14, 1000000000);
}
")

(defn runitnow [] "__result = obj<number>(runit())")

(def now4 (micros))
(def erg4 (runitnow))
(println "Native: " erg4 (- (micros) now4))

;;Call once:  15 +-3
;;Root-runtime-compile:  125 +-10

;;Native interop:  40 +-5
;;Native:  40 +-5
