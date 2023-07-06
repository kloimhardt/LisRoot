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
