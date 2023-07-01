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
  (defn make-types-1 [v]
    (if-not (vector? (second v))
      [(first v) (rest v)]
      [(first v) (into (hash-map) (map make-types-1 (rest v)))]))

  (defn make-types [v]
    (apply hash-map (make-types-1 v)))

  (defn make-syms [n]
    (mapv #(symbol (str "a_" %))  (range n)))

  (defn cvt-to-c [t v]
    (case t
      string (str "string::to<std::string>(" v ").c_str()")
      int (str "number::to<std::int32_t>(" v ")")))

  (defn cvt-from-c [t v]
    (case t
      string (str "obj<string>(" v ")")
      pointer (str "obj<pointer>(" v ")")))

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

(add-types [:Classes
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
             [:B int]
             [Draw
              [:A null]]]])

(defmacro cpp [class method & args]
  (let [m-sub (or (first args) :A)
        funtypes (get-in root-types [:Classes class method m-sub])
        funargs (-> funtypes count make-syms)
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

(println ((cpp TF1 Draw) pf))
(println ((cpp TCanvas Print) pc "demo1_ferret_5.pdf"))


(defmacro ccon [class & args]
  (let [c-sub (or (first args) :A)
        contypes (get-in root-types [:Classes class c-sub])
        funargs (-> contypes count make-syms)
        codestr (str "new "
                     (name class)
                     (argslist (map cvt-to-c contypes funargs)))]
    (list 'fn funargs (wrap-result 'pointer codestr))))

(def pc6 ((ccon TCanvas) "c6" "Something" 0 0 800 600))
(def pf6 ((ccon TF1) "f6" "cos(x)" -5 5))
((cpp TF1 Draw) pf6)
((cpp TCanvas Print) pc6 "demo1_ferret_6.pdf")

;; table for symbols for created classes
