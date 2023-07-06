(defmacro hu [x r ns]
  (defn paren [s] (str "(" s ")"))
  (defn * [& args] (paren (apply str (interpose "*" args))))
  (defn sin [arg] (str "sin" (paren arg)))
  (defn pow [x n] (str "pow" (paren (str x "," n))))
  (defn / [a b] (str a "/" b))
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

(println (hu x 0.2 2))
