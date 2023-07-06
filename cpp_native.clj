(native-header "TCanvas.h")
(native-header "TF1.h")
(require '[c_interop :as c])
(c/load-types "root_types.edn")

(defmacro overload []
  (defn paren [s] (str "(" s ")"))
  (defn * [& args] (paren (apply str (interpose "*" args))))
  (defn sin [arg] (str "sin" (paren arg)))
  (defn pow [x n] (str "pow" (paren (str x "," n))))
  (defn / [a b] (str a "/" b))
  nil)

(overload)

(defmacro nslit-string [x r ns]
  (def pi 3.1415)

  (defn single [x]
    (pow (/ (sin (* pi r x))
            (* pi r x))
         2))

  (defn nslit0 [x]
    (pow (/ (sin (* pi ns x))
            (sin (* pi x)))
         2))

  (* (single x) (nslit0 x)))

(println (nslit-string x 0.2 2))
;; => (pow(sin((3.1415*0.2*x))/(3.1415*0.2*x),2)*pow(sin((3.1415*2*x))/sin((3.1415*x)),2))

(def c ((c/new TCanvas)))

(c/add-type [:Classes TF1] [:B string string int int])
(def Fnslits
  ((c/new TF1 :B) "Fnslits" (nslit-string x 0.2 2) -5 5))

(c/add-type [:Classes TF1 SetNpx] [:A null int])
((c/call TF1 SetNpx) Fnslits 500)

((c/call TF1 Draw) Fnslits)
((c/call TCanvas Print) c "nslits_native.pdf")

(c/add-type [:Classes TF1 Eval]
            [:A null double])

(def now1 (micros))
(def erg ((c/call TF1 Eval) Fnslits 0.4))
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
}")


(defn hu []
  "double x[1] = {3.0};
   double p[1] = {2.0};

__result = obj<number>(nslitfun(x, p))")

(println (hu))
