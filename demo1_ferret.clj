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
   ccc->Print(\"demo1_ferret.pdf\");
  __result = obj<number>(0)")

(def pcc (mc))
(def pff (mf))
(fdraw pff)
(cprint pcc)
