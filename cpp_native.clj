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

(defmacro infix [x r ns]
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

(println (infix x 0.2 2))

(c/add-type [:Classes TF1] [:B string string int int])

(def pf2 ((c/new TF1 :B) "f2" (infix x 0.2 2) -5 5))
(def pc2 ((c/new TCanvas :B) "c2" "Something" 0 0 800 600))

((c/call TF1 Draw) pf2)
((c/call TCanvas Print) pc2 "c_interop_2.pdf")
