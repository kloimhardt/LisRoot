(native-header "TCanvas.h")
(native-header "TF1.h")

(require '[c_interop :as c])
(c/load-types "root_types.edn")

(def pi 3.1415)

(defn single [[x] [r]]
  (pow (/ (sin (* pi r x))
          (* pi r x))
       2))

(defn nslit0 [[x] [r ns]]
  (pow (/ (sin (* pi ns x))
          (sin (* pi x)))
       2))

(defn nslit []
  (fn [x par] (* (single x par) (nslit0 x par))))

(def Fnslit ((c/new TF1) "Fnslit" (nslit) -5.001 5. 2))

(c/add-type [:Classes TF1 SetNpx] [:A null int])

((c/call TF1 SetNpx) Fnslit 500)

((c/call TF1 SetParameters) Fnslit 0.2 2)

(def c ((c/new TCanvas)))

((c/call TF1 Draw) Fnslit)
((c/call TCanvas Print) c "nslit.pdf")

