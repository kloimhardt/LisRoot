(defmacro f1 [& args]
  '(fn [x] x))

((f1 3) 7)

(defmacro f2 [& args]
  `(fn [~@args] (identity 1)))

((f2 vid vnm) 1 2)

(defmacro cvt [] '(fn [t v] v))

((cvt) 1 2)

(defmacro f3 [& args]
  `(fn [~@args] ((cvt) :hu 999)))

(println ((f3 vid vnm) 1 2))

(defmacro regtype1 [name d] `(defn ~name [] ~d))

(regtype1 gettype {:a (list 1 3)})
(println (gettype))

(def types {:TCanvas (list "string" "string" "int" "int" "int" "int")})

(def vid "B")
(def vnm "N")
(def vn1 0)
(def vn2 0)
(def vn3 800)
(def vn4 600)

