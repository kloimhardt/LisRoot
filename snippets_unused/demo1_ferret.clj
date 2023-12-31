(native-header "TF1.h")
(native-header "TCanvas.h")

(defn mint []
  "__result = obj<pointer>(new int(42))")

(defn rint [ipoint]
  "int *i = pointer::to_pointer<int>(ipoint);
  __result = obj<number>(*i)")

(def ii (mint))
(println (rint ii))

(defn mc []
  "__result = obj<pointer>(new TCanvas(\"c\", \"Something\", 0, 0, 800, 600))")

(defn mf []
  "__result = obj<pointer>(new TF1(\"f1\",\"sin(x)\", -5, 5))")

(defn fdraw [ff]
  "TF1 *fff = pointer::to_pointer<TF1>(ff);
   fff->Draw();
  __result = obj<number>(0)")

(defn cprint [cc]
  "TCanvas *ccc = pointer::to_pointer<TCanvas>(cc);
   ccc->Print(\"demo1_ferret_1.pdf\");
  __result = obj<number>(0)")

(def pcc (mc))
(def pff (mf))
(fdraw pff)
(cprint pcc)

(defmacro initfn []
  (defn wrap-fun [args s]
    (list 'fn args s))
  nil)

(initfn)

(defmacro make_canvas []
  (wrap-fun '[] "__result = obj<pointer>(new TCanvas(\"c\", \"Something\", 0, 0, 800, 600))"))

(def pc ((make_canvas)))

(defmacro make_FN1 []
  (wrap-fun '[] "__result = obj<pointer>(new TF1(\"f1\",\"sin(x)\", -5, 5))"))

(def pf ((make_FN1)))

(defmacro draw []
  (wrap-fun '[ff] "pointer::to_pointer<TF1>(ff)->Draw()"))

;;(println ((draw) pf))

(defmacro print []
  (wrap-fun '[cc]
            "pointer::to_pointer<TCanvas>(cc)->Print(\"demo1_ferret_2.pdf\")"))

;;(println ((print) pc))

(defmacro a.. [kw]
  (wrap-fun '[cc]
            (str "pointer::to_pointer<" (name kw) ">(cc)->Print(\"demo1_ferret_3.pdf\")")))

;;((a.. :TCanvas) pc)

(defmacro b.. [& macro_args]
  (wrap-fun '[ptrObj]
            (str "pointer::to_pointer<" (name (first macro_args)) ">(ptrObj)->" (name (second macro_args)) "(\"demo1_ferret_3.pdf\")")))

;;((b.. :TCanvas :Print) pc)

(defmacro some-fns []
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

  (defn make-syms [s n]
    (mapv #(symbol (str s "_" %)) (range n)))

  (defn cvt-to-c [t v]
    (cond
      (= t 'string) (str "string::to<std::string>(" v ").c_str()")
      (= t 'int) (str "number::to<std::int32_t>(" v ")")
      (= t 'double) (str "number::to<double>(" v ")")))

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

  (defn wrap-result [t s]
    (str "__result = " (cvt-from-c t s)))

  (def root-types (hash-map))

  nil)

(some-fns)

(defmacro add-types [t]
  (alter-var-root (var root-types) merge (make-types t))
  nil)

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

(defmacro c-new [class & args]
  (let [c-sub (or (first args) :A)
        contypes (get-in root-types [:Types :Classes class c-sub])
        funargs (->> contypes count (make-syms "a"))
        codestr (str "new "
                     (name class)
                     (argslist (map cvt-to-c contypes funargs)))]
    (list 'fn funargs (wrap-result 'pointer codestr))))

(def pc6 ((c-new TCanvas) "c6" "Something" 0 0 800 600))
(def pf6 ((c-new TF1) "f6" "cos(x)" -5 5))

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

((c-call TF1 Draw) pf6)
(def pdfname "demo1_ferret_6.pdf")
((c-call TCanvas Print) pc6 pdfname)

((c-new TF1) "f6" "cos(x)" -5 5)

(defn mfunone [[x] [par]] (* 2 x))

(defn new-fun [a_0 a_1 a_2 a_3 a_4]
  "__result =
obj<pointer>(new TF1(
string::to<std::string>(a_0).c_str(),

[a_1] (double* vals, double* pars) -> double
{ auto valsl = obj<array_seq<double, number>>(vals, size_t(1));
auto parsl = obj<array_seq<double, number>>(pars, size_t(1));
double res = number::to<double>(run(a_1, valsl, parsl));
return res; },

number::to<double>(a_2),
number::to<double>(a_3),
number::to<double>(a_4)))")

(def pf7 (new-fun "f6" mfunone -5.001 5.0 2))

((c-call TF1 Draw) pf7)
((c-call TCanvas Print) pc6 pdfname)

(defmacro mafun [h]
  (list 'fn '[a_0 a_1 a_2 a_3 a_4]
    "__result =
obj<pointer>(new TF1(
string::to<std::string>(a_0).c_str(),

[a_1] (double* vals, double* pars) -> double
{
return number::to<double>(run(
a_1,
obj<array_seq<double, number>>(vals, size_t(1)),
obj<array_seq<double, number>>(pars, size_t(1))));
},

number::to<double>(a_2),
number::to<double>(a_3),
number::to<double>(a_4)))"))


;; (def pf8 ((mafun 0) "f8" mfunone -5.001 5.0 2)) ;; does not work

(defn aaf [i] (fn [[x]] (* i x)))
(def pf8 ((mafun 0) "f8" (aaf 2) -5.001 5.0 2))

;; works also
;; (defn abf [i] (fn [[x] [par]] (* i x)))
;; (def pf8 ((mafun 0) "f8" (abf 8) -5.001 5.0 2))

((c-call TF1 Draw) pf8)
((c-call TCanvas Print) pc6 "demo1_ferret_8.pdf")

(defmacro morefun []

  (defn c-lambda [varname signature]
    (let [funargs (make-syms "b" (dec (count signature)))
          argstypes (map (fn [e] (if (vector? e) (str (first e) "*") e))
                         (rest signature))
          combined (map (fn [t v] (str t " " v)) argstypes funargs)]
      (str "[" varname "] " (argslist combined) " -> " (first signature) " {}")))

  (str (c-lambda "a_1" (get-in root-types [:Types :Functions :plot-function])))
  ;;
)

(println (morefun))

(defmacro evenmorefun []

  (defn c-lambdabody [funname signature]
    (str "return "
         (cvt-to-c (first signature)
                   (str "run"
                        (argslist
                          (cons
                            funname
                            (map cvt-from-c
                                 (rest signature)
                                 (make-syms "b" (dec (count signature))))))))
         ";"))

  (defn c-lambda-1 [varname signature]
    (let [funargs (make-syms "b" (dec (count signature)))
          argstypes (map (fn [e] (if (vector? e) (str (first e) "*") e))
                         (rest signature))
          combined (map (fn [t v] (str t " " v)) argstypes funargs)]
      (str "[" varname "] " (argslist combined) " -> " (first signature)
           " {" (c-lambdabody varname signature) "}")))

  (defn cvt-to-c-1 [t v]
    (cond
      (= t 'string) (str "string::to<std::string>(" v ").c_str()")
      (= t 'int) (str "number::to<std::int32_t>(" v ")")
      (= t 'double) (str "number::to<double>(" v ")")
      (keyword? t) (c-lambda-1 v (get-in root-types [:Types :Functions t]))))

nil)

(evenmorefun)

(defmacro c-new-1 [class & args]
  (let [c-sub (or (first args) :A)
        contypes (get-in root-types [:Types :Classes class c-sub])
        funargs (->> contypes count (make-syms "a"))
        codestr (str "new "
                     (name class)
                     (argslist (map cvt-to-c-1 contypes funargs)))]
    (list 'fn funargs (wrap-result 'pointer codestr))))

(defn aaff [i] (fn [[x]] (* i x)))

(def pf9 ((c-new-1 TF1 :B) "f9" (aaff 5) -5.001 5.0 2))

((c-call TF1 Draw) pf9)
((c-call TCanvas Print) pc6 "demo1_ferret_8.pdf")

(comment

  (def uu (get-in root-types [:Types :Functions :plot-function]))
;; => (double [double 1] [double 1])

  (c-lambda "a_1" uu)
;; => "[a_1] (double* b_0, double* b_1) -> double {}"

  (def aa (get-in root-types [:Types :Classes 'TF1 :B]))
;; => (string :plot-function double double double)

  (defn c-lambda-1 [varname signature]
    (let [funargs (make-syms "b" (dec (count signature)))
          argstypes (map (fn [e] (if (vector? e) (str (first e) "*") e))
                         (rest signature))
          combined (map (fn [t v] (str t " " v)) argstypes funargs)]
      (str "[" varname "] " (argslist combined) " -> " (first signature)
           " {" (c-lambdabody varname signature) "}")))

  (defn cvt-to-c-1 [t v]
    (cond
      (= t 'string) (str "string::to<std::string>(" v ").c_str()")
      (= t 'int) (str "number::to<std::int32_t>(" v ")")
      (= t 'double) (str "number::to<double>(" v ")")
      (keyword? t) (c-lambda-1 v (get-in root-types [:Types :Functions t]))))

  (defmacro c-new-1 [class & args]
    (let [c-sub (or (first args) :A)
          contypes (get-in root-types [:Types :Classes class c-sub])
          funargs (->> contypes count (make-syms "a"))
          codestr (str "new "
                       (name class)
                       (argslist (map cvt-to-c-1 contypes funargs)))]
      (list 'fn funargs (wrap-result 'pointer codestr))))

  (defn aaff [i] (fn [[x]] (* i x)))

  ((c-new-1 TF1 :B) "f8" (aaff 2) -5.001 5.0 2)

  )

