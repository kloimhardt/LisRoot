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

(println ((draw) pf))

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
  (def typevector [:Classes
                   [:TCanvas
                    [:A :string :string :int :int :int :int]
                    [:B :int]
                    [:Print
                     [:A :string]
                     [:B :int]]
                    [:TCanvasMethod2
                     [:A :int]
                     [:B :string]]]
                   [:TF1
                    [:A :string]
                    [:B :int]
                    [:Darw
                     [:A]]]])

  (defn wrap-result [s]
    (str "__result = " "obj<number>(" s ");"))

  (defn make-types-1 [v]
    (if-not (vector? (second v))
      [(first v) (rest v)]
      [(first v) (into (hash-map) (map make-types-1 (rest v)))]))

  (defn make-types [v]
    (apply hash-map (make-types-1 typevector)))

  (def types (make-types typevector))

  (defn make-syms [n]
    (mapv #(symbol (str "a_" %))  (range n)))

  (defn cvt [t v]
    (case t
      :string (str "string::to<std::string>(" v ").c_str()")))

  (defn argslist [strs]
    (str "(" (apply str (interpose ", " strs)) ")"))

  nil)

(some-fns)

(defmacro cpp [class-kw method-kw m-sub]
  (let [funargs (-> types
                    (get-in [:Classes class-kw method-kw m-sub])
                    count
                    inc
                    make-syms)]
    (list 'fn funargs
          (str "pointer::to_pointer<"
               (name class-kw)
               ">("
               (first funargs)
               ")->"
               (name method-kw)
               (argslist (map cvt [:string] (rest funargs)))))))

((cpp :TCanvas :Print :A) pc "demo1_ferret_4.pdf")
