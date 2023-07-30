(defmacro malli-fns []
  (def maps-to-vector
    (fn [m]
      (cond
        (map? m) (maps-to-vector (cons :map m))
        (coll? m) (mapv maps-to-vector m)
        :else m)))

  (def remove-kw-ns
    (fn [m]
      (cond
        (vector? m) (mapv remove-kw-ns m)
        (qualified-keyword? m) (keyword "lisc" (name m))
        :else m)))

  (def vector-to-maps
    (fn [m]
      (cond
        (and (vector? m) (= :map (first m)))
        (into (hash-map) (vector-to-maps (rest m)))
        (coll? m)
        (mapv vector-to-maps m)
        :else
        m)))

  (def malli-to-map (comp vector-to-maps remove-kw-ns maps-to-vector))

  nil)

(malli-fns)

(defmacro type-fns []
  (def malli-types (volatile! (hash-map)))

  (def m-set-types-raw
    (fn [t]
      (vreset! malli-types (malli-to-map t))))

  (def m-add-type-raw
    (fn [path t]
      (let [sub-type (first t)
            lasttwo (take-last 2 t)
            ret-arg (if (= (first lasttwo) :->)
                      [(last t) (rest (drop-last 2 t))]
                      [:nil (rest t)])
            malli-t (concat (vector :cat)
                            (second ret-arg)
                            (when-not (= :nil (first ret-arg))
                              (vector [:= (first ret-arg)])))]
        (vswap! malli-types
                assoc-in
                (concat path (list sub-type))
                malli-t))))

  nil)

(type-fns)

(defmacro m-load-types [filename]
  (m-set-types-raw (read-string (slurp filename)))
  nil)

(defmacro m-add-type [path t]
  (m-add-type-raw path t)
  nil)

(defmacro class-fns []
  (def make-syms
    (fn [s n]
      (mapv #(symbol (str s "_" %)) (range n))))

  (def cvt-from-c
    (fn [t v]
      (cond
        (= t :string) (str "obj<string>(" v ")")
        (= t :pointer) (str "obj<pointer>(" v ")")
        (= t :double) (str "obj<number>(" v ")")
        (and (vector? t) (= :vector (first t)))
        (str "obj<array_seq<"
             (name (last t))
             ", number>>("
             v
             ", size_t("
             (get (second t) :max)

             "))")
        :else v)))

  (def argslist
    (fn [strs]
      (str "(" (apply str (interpose ", " strs)) ")")))

  (def cvts-to-c
    (fn [t v]
      (cond
        (= t :string) (str "string::to<std::string>(" v ").c_str()")
        (= t :int) (str "number::to<std::int32_t>(" v ")")
        (= t :double) (str "number::to<double>(" v ")")
        (= t :lisc/int-to-double) (str "number::to<double>(" v ")")
        (and (vector? t) (= :enum (first t))) (str (second t))
        :else v)))

  (def c-lambdabody
    (fn [funname signature]
      (str "return "
           (cvts-to-c (first signature)
                      (str "run"
                           (argslist
                             (cons
                               funname
                               (map cvt-from-c
                                    (rest signature)
                                    (make-syms "b" (dec (count signature))))))))
           ";")))

  (def m-c-lambda
    (fn [varname malli-signature]
      (let [signature (cons (second (last malli-signature))
                            (butlast (rest malli-signature)))
            funargs (make-syms "b" (dec (count signature)))
            argstypes (map (fn [e] (if (vector? e) (str (name (last e)) "*")
                                       (name e)))
                           (rest signature))
            combined (map (fn [t v] (str t " " v)) argstypes funargs)]
        (str "[" varname "] " (argslist combined) " -> " (name (first signature))
             " {" (c-lambdabody varname signature) "}"))))

  (def cvt-to-c
    (fn [native-string]
      (fn [t v]
        (cond
          (= :lisc/native-string t)
          native-string
          (= :lisc/plot-function t)
          (m-c-lambda v (get-in (deref malli-types) [:registry t]))
          (and (keyword? t) (re-matches #"R\dR\d->R" (name t)))
          (m-c-lambda v (get-in (deref malli-types) [:registry t]))
          :else
          (cvts-to-c t v)))))

  (def wrap-result
    (fn [t s]
      (str "__result = " (cvt-from-c t s))))

  (def new-raw
    (fn [class args]
      (let [m-c-sub (or (first args) :default)
            native-string (second args)
            m-contypes (next (get-in (deref malli-types) [(keyword class) m-c-sub]))
            m-funargs (->> m-contypes count (make-syms "a"))
            real-funargs m-funargs
            m-codestr (str "new "
                           (name class)
                           (argslist (map (cvt-to-c native-string) m-contypes m-funargs)))
            m-funcode (list 'fn real-funargs (wrap-result :pointer m-codestr))
            m-erg (vector (if (seq real-funargs) :fn :new-no-args)
                          m-funcode)]
        m-erg)))

(def call-raw
    (fn [class method args]
      (let [m-m-sub (or (first args) :default)
            native-string (second args)
            m-types (get-in (deref malli-types)
                            [(keyword class) (keyword method) m-m-sub])
            m-funtypes (next m-types)
            m-lasttwo (take-last 2 m-funtypes)
            m-ret-arg (if (and (vector? (last m-funtypes))
                                   (= := (first (last m-funtypes))))
                        [(second (last m-funtypes)) (butlast m-funtypes)]
                        [:nil m-funtypes])
            m-arg-types (second m-ret-arg)
            m-arg-symbols (->> m-arg-types count inc (make-syms "a"))
            m-codestr (str "pointer::to_pointer<"
                           (name class)
                           ">("
                           (first m-arg-symbols)
                           ")->"
                           (name method)
                           (argslist (map (cvt-to-c native-string)
                                          m-arg-types
                                          (rest m-arg-symbols))))
            m-erg (list 'fn m-arg-symbols
                        (if (= (first m-ret-arg) :nil)
                          m-codestr
                          (wrap-result
                            (first m-ret-arg)
                            m-codestr)))]
        (vector :fn m-erg))))

  (def stri
    (fn [x]
      (if (coll? x) (cons 'list (map stri x))
          (str x))))

  (def interop-fn
    (fn [macargs c-function m-data]
      (list 'fn [(symbol "&") 'args]
            (list 'checkit
                  (stri macargs)
                  (stri m-data)
                  'args)
            (list 'apply (second c-function) 'args))))

  (def interop-fn-direct
    (fn [macargs c-function m-data]
      (cond
        (= :new-no-args (first c-function))
        (list 'do
              (list 'checkit
                    (stri macargs)
                    (stri m-data)
                    (list 'list))
              (list (second c-function)))
        :else
        (interop-fn macargs c-function m-data))))

  (def interop-vec
    (fn [macargs]
      (let [method (first macargs)
            class (second macargs)
            types (first (nnext macargs))
            m-types-kw (if (vector? types) :default (or types :default))
            r (next (nnext macargs))]
        (do
          (cond
            (and (vector? types) (= (symbol "new") method))
            (m-add-type-raw (list (keyword class)) (map keyword (cons m-types-kw types)))
            (vector? types)
            (m-add-type-raw (map keyword [class method]) (map keyword (cons m-types-kw types))))
          (let [m-data (if (= (symbol "new") method)
                         (get-in (deref malli-types) [(keyword class) m-types-kw])
                         (get-in (deref malli-types) [(keyword class)
                                              (keyword method)
                                              m-types-kw]))
                c-function (cond
                             (= (symbol "bless") method)
                             (vector :fn 'identity)
                             (= (symbol "new") method)
                             (new-raw class (cons m-types-kw r))
                             :else
                             (call-raw class method (cons m-types-kw r)))]
            (vector macargs c-function m-data))))))

  (def interop-flat
    (fn [macargs]
      (apply interop-fn (interop-vec macargs))))

  (def interop
    (fn [macargs]
      (apply interop-fn-direct (interop-vec macargs))))

  (def _doto
    (fn [args]
      (let [frt (first args)
            frt1 (if (= (symbol "new") (first frt)) (list frt) frt)
            hack-frt (if (and (= (symbol "TF1") (second (first frt1)))
                              (= :XR2 (first (nnext (first frt1)))))
                       (update (vec frt1) 2 (fn [x] (list 'identity x)))
                       frt1)
            ;; hack for https://github.com/nakkaya/ferret/issues/52
            a (cons hack-frt (rest args))

            b (map (fn [x] (if-not (coll? x) (list x) x))
                   a)
            c (map (fn [x] (if-not (coll? (first x))
                             (cons (list (first x)) (rest x))
                             x))
                   b)
            class (second (ffirst c))
            d (cons (interop-flat (ffirst c)) (rest (first c)))
            e (map (fn [x] (cons (interop-flat (concat (list (ffirst x) class)
                                                       (rest (first x))))
                                 (rest x)))
                   (rest c))
            f (cons d e)
            erg (cons 'doto f)]
        erg)))
  nil)

(class-fns)

(comment

  (m-load-types "malli_types.edn")

  (identity _m-contypes)

  (identity _op)
  (identity _oo)
  (new-raw 'TF1 [:XR2])

  (new-raw 'TCanvas [])

  ;;
  )



(defn not-double? [v]
  (and (not (zero? v)) (zero? (dec (inc v)))))

(defn not-int? [v]
  (not= (floor v) v))

(defn check-value [type v]
  (list
    (cond
      (= type ":double") (if (not-double? v) "-" "+")
      (= type ":int") (if (not-int? v)  "-" "+")
      (= type ":lisc/int-to-double") (if (not-int? v) "-" "+")
      (= type ":string") (if-not (string? v) "-" "+")
      (= type ":lisc/plot-function") "!"
      (= type ":lisc/R1R2->R") "!"
      (= type ":lisc/instance") "!"
      :else "?")
    type v))

(defn check-count [types args]
  (list (if (not= (count types) (count args))  "-" "+")
        "count"))

(defn checkit [macargs types args]
  ;; (println "------ checkit" macargs)
  ;; (println types args)
  (let [lasttype (nth types (dec (count types)))
        types-args (cond
                     (= (first macargs) "new")
                     (list (rest types) args)
                     (and (list? lasttype) (= (first lasttype) ":="))
                     (list (cons ":lisc/instance"
                                 (rest (take (dec (count types)) types )))
                           args)
                     :else
                     (list (cons ":lisc/instance" (rest types)) args))]
    (print (filter (fn [x] (= (first x) "-"))
                         (check-count (first types-args) (second types-args))))
    (print (filter (fn [x] (= (first x) "-"))
                         (map check-value (first types-args) (second types-args))))))

(defmacro new [class & args]
  (let [fncode (new-raw class args)]
    (if (= :new-no-args (first fncode))
      (list (second fncode))
      (second fncode))))

(defmacro call [class method & args]
  (second (call-raw class method args)))

(defmacro defnative [head body]
  (list 'native-declare (str head "{return " (eval body) ";}")))

(defmacro _ [& args] (interop args))

(defmacro > [& args] (_doto args))
(defmacro T [& args] (_doto args))

(m-load-types "malli_types.edn")
