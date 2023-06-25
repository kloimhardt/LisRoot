(native-header "TF1.h")
(native-header "TCanvas.h")

(defn positive-numbers
  ([]
   (positive-numbers 1))
  ([n]
   (cons n (lazy-seq (positive-numbers (inc n))))))

(println (->> (positive-numbers)
              (take 5)
              (apply +)))

(defn makeplot1 []
  "TCanvas* c = new TCanvas(\"c\", \"Something\", 0, 0, 800, 600);
  TF1 *f1 = new TF1(\"f1\",\"sin(x)\", -5, 5);
  f1->SetLineColor(kBlue+1);
  f1->SetTitle(\"My graph;x; sin(x)\");
  f1->Draw();
  c->Print(\"basicplot_1.pdf\");
  __result =  obj<number>(0);")

(makeplot1)

(defn makeplot2 [fstr]
  "const char* c_fstr = string::to<std::string>(fstr).c_str();
  TCanvas* c = new TCanvas(\"c\", \"Something\", 0, 0, 800, 600);
  TF1 *f1 = new TF1(\"f1\",c_fstr, -5, 5);
  f1->SetLineColor(kBlue+1);
  f1->SetTitle(\"My graph;x; sin(x)\");
  f1->Draw();
  c->Print(\"basicplot_2.pdf\");
  __result =  obj<number>(0);")

(makeplot2 "sin(x)")


(defn mfun [x par] (* par x))

(defn callfun1 [afun a b]
  "__result =  run(afun,a,b);")

(println (callfun1 mfun 3.0 4.0))

(defn callfun2 []
  "auto cfun = [](double x, double par){ return x * par; };
   __result = obj<number>(cfun(5.0, 6.0));")

(println (callfun2))

(defn callfun3 [afun]
  "auto cfun = [afun](double x, double par){ return run(afun, obj<number>(x), obj<number>(par)); };
   __result = cfun(5.0, 7.0);")

(println (callfun3 mfun))

(defn makelist []
  "double buff[3] = {1.6,2.7,3.3};
   __result = obj<array_seq<double, number>>(buff, size_t(3));"
  )

(println (makelist))

(defn mfun-array [[x y] [par1 par2]] (+ (* x par1) (* y par2)))

(defn callfun4 [afun, vals_dim, pars_dim]
  "auto cvdim = number::to<std::int8_t>(vals_dim);
   auto cpdim = number::to<std::int8_t>(pars_dim);
   auto cfun = [afun, cvdim, cpdim](double* vals, double* pars)
               { auto valsl = obj<array_seq<double, number>>(vals, size_t(cvdim));
                 auto parsl = obj<array_seq<double, number>>(pars, size_t(cpdim));
                 return run(afun, valsl, parsl); };
   double a[2] = {2.0, 3.0};
   double b[2] = {4.0, 5.0};
   __result = cfun(a, b);")

(println (callfun4 mfun-array 2 2))

(defn makeplot3 [afun, vals_dim, pars_dim]
  "auto cvdim = number::to<std::int8_t>(vals_dim);
   auto cpdim = number::to<std::int8_t>(pars_dim);
   auto cfun = [afun, cvdim, cpdim] (double* vals, double* pars) -> double
               { auto valsl = obj<array_seq<double, number>>(vals, size_t(cvdim));
                 auto parsl = obj<array_seq<double, number>>(pars, size_t(cpdim));
                 double res = number::to<double>(run(afun, valsl, parsl));
                 return res; };
  TCanvas* c = new TCanvas(\"c\", \"Something\", 0, 0, 800, 600);
  TF1 *f1 = new TF1(\"f1\", cfun, -5.001,5.,2);
  f1->SetLineColor(kBlue+1);
  f1->SetTitle(\"My graph;x; sin(x)\");
  f1->Draw();
  c->Print(\"basicplot_3.pdf\");
  __result =  obj<number>(0);")

(defn mfunone [[x] [par]] (* 2 x))

(makeplot3 mfunone 1 1)
