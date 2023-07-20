(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/load-types "root_types.edn")
(c/m-load-types "malli1.edn")

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

(def c (c/new TCanvas))

(c/add-type [:Classes TF1] [:B string string int int])
(c/m-add-type [:TF1] [:B :string :string :int :int])

(def Fnslits ((c/new TF1 :B) "Fnslits" (nslit "x" 0.2 2) -5 5))

(c/add-type [:Classes TF1 SetNpx] [:A int])
(c/m-add-type [:TF1 :SetNpx] [:A :int])
((c/call TF1 SetNpx) Fnslits 500)

((c/call TF1 Draw) Fnslits)
((c/call TCanvas Print) c "nslits_native.pdf")

(c/add-type [:Classes TF1 Eval] [:A double -> double])
(c/m-add-type [:TF1 :Eval] [:A :double :-> :double])

(def now1 (micros))
(def erg1 ((c/call TF1 Eval) Fnslits 0.4))
(println "Call once: " erg1 (- (micros) now1))

(c/add-type [:Classes TF1 GetX]
            [:A double double double double int -> double])

(c/m-add-type [:TF1 :GetX]
              [:A :double :double :double :double :int :-> :double])

(def now (micros))
(def erg ((c/call TF1 GetX) Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Root-runtime-compile: " erg (- (micros) now))

(c/defnative "double cpp_nslit(double* x, double* par)"
  ((* single nslit0) "x[0]" 0.2 2))

(def FastSlits ((c/new TF1 :native cpp_nslit) "Fnslit" "native" -5.001 5. 2))

(def now2 (micros))
(def erg2 ((c/call TF1 GetX) FastSlits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Native interop: " erg2 (- (micros) now2))

((c/call TF1 Draw) FastSlits)
((c/call TCanvas Print) c "nslits_fast.pdf")

(native-declare
  "double nslitfun(double* x, double* par){
  return pow(sin((3.1415*0.2*x[0]))/(3.1415*0.2*x[0]),2)*pow(sin((3.1415*2*x[0]))/sin((3.1415*x[0])),2);
}

double runit() {
  TF1 *Fnslit  = new TF1(\"Fnslit\",nslitfun,-5.001,5.,2);
  return Fnslit->GetX(3.6, -5.0, 0.3, 1.E-14, 1000000000);
}
")

(defn runitnow [] "__result = obj<number>(runit())")

(def now3 (micros))
(def erg3 (runitnow))
(println "Native: " erg3 (- (micros) now3))

;;Call once:  15 +-3
;;Root-runtime-compile:  125 +-10

;;Native interop:  40 +-5
;;Native:  40 +-5
