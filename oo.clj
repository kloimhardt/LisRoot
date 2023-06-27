(defmacro f1 [& args]
  '(fn [x] x))

(println ((f1 3) 7))

(defmacro f2 [& args]
  `(fn [~@args] (identity 1)))

(println ((f2 vid vnm) 1 2))

(defmacro cvt1 [] '(fn [t v] v))

(println ((cvt1) 1 2))

(defmacro f3 [& args]
  `(fn [~@args] ((cvt1) :hu 999)))

(println ((f3 vid vnm) 1 2))

(defmacro regtype1 [name d] `(defn ~name [] ~d))

(regtype1 gettype {:a (list 1 3)})
(println (gettype))

(defmacro n1 []
  `(fn [& x#]
     x#))

(println ((n1) 666))

(defmacro types [] '{:myobj (list :TCanvas :string :int)})

(println (types))


(defmacro n2 [t tps]
  `(fn [& x#]
     (map (cvt1) (get ~tps ~t) x#)))

(println ((n2 :myobj (types)) "s" 1))


(defn hu [x] x)

(defmacro ha [] '(hu 3))

(println (ha))

(comment

  (def uu (:myobj (types)))

  (str (name (first uu))
       "* o = new "
       (name (first uu))
       (->> uu rest (map str) (interpose ", ") (into (list))))


(defmacro types2 [] '{:myobj (list "TCanvas" "string" "int")})

(defmacro n3_1 [t tps]
  `(fn [& args#]
     (str (name (first args#))
          "* o = new "
          (name (first args#))
          (->> args# rest (map str) (interpose ", ") (into (list))))))

#_((n3_1 :myobj (types)) "MmyClass" "theObjName")

(defmacro n3 [t tps & args]
  `((n3_1 ~t ~tps) ~@args))

(n3 :myobj (types) "MmyClass" "theObjName")

)

(defn io1 [] "__result = obj<number>(9 + 9)")

(println (io1))

(defn io3 [] (cxx "__result = obj<number>(9 + 1)"))

(println (io3))

(defmacro st [] "__result = obj<number>(9 + 2)")

(println (st))

(defn fu [x] (new-string (concat x "noch")))

(defmacro io4 [sti] `(fu ~sti))

(println (io4 (st)))

(println (cxx "__result = obj<number>(4 + 2)"))

;; does not work
;; (println (cxx (st)))

(defmacro fum [x] `(new-string (concat ~x "noch")))

(println (fum "da-"))

(defmacro io5 [sti] `(fum ~sti))

(println (io5 "dort"))

(defmacro io6 [sti] `(cxx ~sti))

(println (io6 "__result = obj<number>(4 + 3)"))

(def cde "__result = obj<number>(4 + 1)")

;; does not work
;; (defmacro io6 [sti] `(cxx ~cde))
;; (println (io6 "__result = obj<number>(4 + 3)"))

(defmacro h1 [] `(new-string "__result = obj<number>(2 + 1)"))

(h1)

(defn nnf [] "__result = obj<number>(4 + 1)")

(println (nnf))

(defmacro tru [name] `(defn ~name [] "__result = obj<number>(4 - 1)"))

(tru nf)

(println (nf))

(defmacro cdef [] "__result = obj<number>(3 + 13);")

(defmacro tru1 [name] `(defn ~name [] (cdef)))

(tru1 nf1)

(println (nf1)) ;; gives 16

(def cdef2 "__result = obj<number>(3 + 11);")

(defmacro tru2 [name] `(defn ~name [] cdef2))

(tru2 nf2)

(println (nf2)) ;;gives a string

(defn cdef3 [] (new-string "__result = obj<number>(3 + 12);"))

(defmacro tru3 [name] `(defn ~name [] (cdef3)))

(tru3 nf3)

(println (nf3)) ;;gives a string

(defmacro cdef4 [] `(new-string "__result = obj<number>(3 + 14);"))

(defmacro tru4 [name] `(defn ~name [] (cdef4)))

(tru4 nf4)

(println (nf4)) ;;gives a string :-(


(defmacro cdef5 [] `(new-string "__result = obj<number>(3 + 17);"))

(defmacro tru5 [name] `(defn ~name [] (cdef5)))

(tru5 nf5)

(println (nf5)) ;;gives a string :-(

(defmacro uuu [] (identity "__result = obj<number>(3 + 18);"))

(defmacro tru6 [name] `(defn ~name [] (uuu)))

(tru6 nf6)

(println (nf6)) ;;number 21 !!

(defn hiu [x y] x)

(defmacro cdef7 []
  (str "__result = " "obj<number>(4 + 19);")) ;; that is Clojure

(defmacro tru7 [name] `(defn ~name [] (cdef7)))

(tru7 nf7)

(println (nf7))
