(native-header "TF1.h")
(native-header "TCanvas.h")

(defmacro type-fns []
  (defn form-arrays [v]
    (->> v
         (partition-all 2 1)
         (remove #(vector? (first %)))
         (map (fn [v]
                (let [o (first v)
                      t (second v)]
                  (if (vector? t)
                    (vector o (first t))
                    o))))))

  (defn make-types-1 [v]
    (if (every? vector? (rest v))
      [(first v) (into (hash-map) (map make-types-1 (rest v)))]
      [(first v) (form-arrays (rest v))]))

  (defn make-types [v]
    (apply hash-map (make-types-1 v)))

  (def root-types (hash-map))

  nil)

(type-fns)

(defmacro add-types [t]
  (alter-var-root (var root-types) merge (make-types t))

  nil)

(defmacro class-fns []
  (defn make-syms [s n]
    (mapv #(symbol (str s "_" %)) (range n)))

  (defn cvt-from-c [t v]
    (cond
      (= t 'string) (str "obj<string>(" v ")")
      (= t 'pointer) (str "obj<pointer>(" v ")")
      (vector? t) (str "obj<array_seq<"
                       (first t)
                       ", number>>("
                       v
                       ", size_t("
                       (second t)

                       "))")))
  (defn argslist [strs]
    (str "(" (apply str (interpose ", " strs)) ")"))

  (defn cvts-to-c [t v]
    (cond
      (= t 'string) (str "string::to<std::string>(" v ").c_str()")
      (= t 'int) (str "number::to<std::int32_t>(" v ")")
      (= t 'double) (str "number::to<double>(" v ")")))

  (defn c-lambdabody [funname signature]
    (str "return "
         (cvts-to-c (first signature)
                    (str "run"
                         (argslist
                           (cons
                             funname
                             (map cvt-from-c
                                  (rest signature)
                                  (make-syms "b" (dec (count signature))))))))
         ";"))

  (defn c-lambda [varname signature]
    (let [funargs (make-syms "b" (dec (count signature)))
          argstypes (map (fn [e] (if (vector? e) (str (first e) "*") e))
                         (rest signature))
          combined (map (fn [t v] (str t " " v)) argstypes funargs)]
      (str "[" varname "] " (argslist combined) " -> " (first signature)
           " {" (c-lambdabody varname signature) "}")))

  (defn cvt-to-c [t v]
    (cond
      (keyword? t) (c-lambda v (get-in root-types [:Types :Functions t]))
      :else (cvts-to-c t v)))

  (defn wrap-result [t s]
    (str "__result = " (cvt-from-c t s)))

  nil)

(class-fns)

(defmacro c-new [class & args]
  (let [c-sub (or (first args) :A)
        contypes (get-in root-types [:Types :Classes class c-sub])
        funargs (->> contypes count (make-syms "a"))
        codestr (str "new "
                     (name class)
                     (argslist (map cvt-to-c contypes funargs)))]
    (list 'fn funargs (wrap-result 'pointer codestr))))

(defmacro c-call [class method & args]
  (let [m-sub (or (first args) :A)
        funtypes (get-in root-types [:Types :Classes class method m-sub])
        funargs (->> funtypes count (make-syms "a"))
        codestr (str "pointer::to_pointer<"
                     (name class)
                     ">("
                     (first funargs)
                     ")->"
                     (name method)
                     (argslist (map cvt-to-c (rest funtypes) (rest funargs))))]
    (list 'fn funargs
          (if (= (first funtypes) 'null)
            codestr
            (wrap-result (first funtypes) codestr)))))

(add-types [:Types
            [:Classes
             [TCanvas
              [:A string string int int int int]
              [:B int]
              [Print
               [:A null string]
               [:B null int]]
              [TCanvasMethod2
               [:A int]
               [:B string]]]
             [TF1
              [:A string string int int]
              [:B string :plot-function double double double]
              [Draw
               [:A null]]]]
            [:Functions
             [:plot-function double double[1] double[1]]]])

(def pc1 ((c-new TCanvas) "c1" "Something" 0 0 800 600))
(def pf1 ((c-new TF1) "f1" "cos(x)" -5 5))
((c-call TF1 Draw) pf1)
((c-call TCanvas Print) pc1 "c_interop_1.pdf")
