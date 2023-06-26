(native-header "TF1.h")
(native-header "TCanvas.h")

(println "hello world")
(def m {:a 2 :b 4})
(println (:b m))

(defn make_canvas []
  "TCanvas* c = new TCanvas(\"c\", \"Something\", 0, 0, 800, 600);
   __result = obj<pointer>(c);")

(println (make_canvas))

(defn use_function1 []
  "std::function<double(double)> fun = [](double x) { return 3.0*x;};
   __result = obj<number>(fun(4.0));")

(println (use_function1))

(println (conj (list 2 3 4) 1))

(require '[lib :as l])

(println (l/hu))

(defn ha [& args] args)

(println (ha 5 6 7))

(defn c_string [s]
  (l/add-strs "string::to<std::string>(" s ").c_str()"))

(println (c_string "i"))

