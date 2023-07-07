(native-header "TCanvas.h")
(native-header "TF1.h")
(require '[c_interop :as c])
(c/load-types "root_types.edn")

(defmacro overload []
  (defn paren [s] (str "(" s ")"))
  (defn * [& args] (apply str (interpose "*" args)))
  (defn sin [arg] (str "sin" (paren arg)))
  (defn pow [x n] (str "pow" (paren (str x "," n))))
  (defn / [a b] (str a "/" (paren b)))
  nil)

(overload)

(defmacro make-expression [r ns]
  (def pi 3.1415)

  (defn single [x]
    (pow (/ (sin (* pi r x))
            (* pi r x))
         2))

  (defn nslit0 [x]
    (pow (/ (sin (* pi ns x))
            (sin (* pi x)))
         2))

  (defn nslit [x]
    (* (single x) (nslit0 x)))

  (nslit "x"))

(def nslit-string (make-expression 0.2 2))

(println nslit-string)
;;=> pow(sin(3.1415*0.2*x)/(3.1415*0.2*x),2)*pow(sin(3.1415*2*x)/(sin(3.1415*x)),2)

(def c ((c/new TCanvas)))

(c/add-type [:Classes TF1] [:B string string int int])
(def Fnslits
  ((c/new TF1 :B) "Fnslits" nslit-string -5 5))

(c/add-type [:Classes TF1 SetNpx] [:A null int])
((c/call TF1 SetNpx) Fnslits 500)

((c/call TF1 Draw) Fnslits)
((c/call TCanvas Print) c "nslits_native.pdf")

(c/add-type [:Classes TF1 Eval]
            [:A null double])

(def now1 (micros))
(def erg1 ((c/call TF1 Eval) Fnslits 0.4))
(println "Basetime: " (- (micros) now1))

(c/add-type [:Classes TF1 GetX]
            [:A null double double double double int])

(def now (micros))
(def erg ((c/call TF1 GetX) Fnslits 3.6 -5.0 0.3 1.E-14 1000000000))
(println "Calctime: " (- (micros) now))

;;Basetime:  15 +-3
;;Calctime:  125 +-10

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

(def now2 (micros))
(def erg2 (runitnow))
(println "Calctime2: " (- (micros) now2))

;;Calctime2:  45 +-5

(defmacro def-native-fn-str []
  (def native-fn-str
    (str
      "[] (double* x, double* par) -> double {
return " (nslit "x[0]") ";}"))
  nil)

(def-native-fn-str)

(def FastSlits
  ((c/new TF1
          :native
          native-fn-str
          )
   "Fnslit" "dum" -5.001 5. 2))

((c/call TF1 Draw) FastSlits)
((c/call TCanvas Print) c "nslits_fast.pdf")

