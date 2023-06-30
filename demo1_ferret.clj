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
  (wrap-fun '[ff]
            "TF1 *fff = pointer::to_pointer<TF1>(ff);
fff->Draw();
__result = obj<number>(0)")

((draw) pf)

(defmacro print []
  (wrap-fun '[cc]
            "TCanvas *ccc = pointer::to_pointer<TCanvas>(cc);
ccc->Print(\"demo1_ferret_2.pdf\");
__result = obj<number>(0)"))

((print) pc)
