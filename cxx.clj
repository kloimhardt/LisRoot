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
        (and (vector? m) (= :rtm (first m)))
        m
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

  (def add-type-rtm-cxx
    (fn [path]
      (vswap! malli-types update-in (butlast path)
              (fn [x] (assoc x :default (get x (last path)))))))

  nil)

(type-fns)

(defmacro Ts-default [path]
  (add-type-rtm-cxx path)
  nil)

(defmacro m-load-types [str-types str-defaults]
  (m-set-types-raw (read-string (slurp str-types)))
  (run! add-type-rtm-cxx (read-string (slurp str-defaults)))
  nil)

(defmacro m-add-type [path t]
  (m-add-type-raw path t)
  nil)

(defmacro class-fns []
  (def make-syms
    (fn [s n]
      (mapv #(symbol (str s "_" %)) (range n))))

  (def obj-encode
    (fn [s p]
      (str "__result = rt::dense_list(obj<string>(\"" s "\"), obj<pointer>(" p "))")))

  (def obj-decode
    (fn [dense-list]
      (str "sequence::to<std_vector>(" dense-list ")[1]")))

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

  (def given-value
    (fn [typ args]
      (when (and (vector? typ) (= :enum (first typ)))
        (if (= :args (last typ))
          (nth args (second typ))
          (second typ)))))

  (def cvts-to-c
    (fn [t v]
      (cond
        (= t :string) (str "string::to<std::string>(" v ").c_str()")
        (= t :int) (str "number::to<std::int32_t>(" v ")")
        (= t :double) (str "number::to<double>(" v ")")
        (= t :sequence) (str "sequence::to<std_vector>(" v ")")
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

  (def get-malli-types
    (fn [path] (get-in (deref malli-types) path)))

  (def get-ct-malli-types
    (fn [path]
      (let [scheme (get-malli-types path)]
        (if (map? scheme) (get scheme :cxx) scheme))))

  (def cvt-to-c
    (fn [args]
      (fn [t v]
        (let [funspec (get-ct-malli-types [:registry t])]
          (cond
            (given-value t args)
            (str (given-value t args))
            (and (vector? funspec) (= [:= :double] (last funspec)))
            (m-c-lambda v funspec)
            :else
            (cvts-to-c t v))))))

  (def wrap-result
    (fn [t s]
      (str "__result = " (cvt-from-c t s))))

  (def new-raw
    (fn [class args]
      (let [m-c-sub (or (first args) :default)
            m-contypes (next (get-ct-malli-types [(keyword class) m-c-sub]))
            m-funargs (->> m-contypes count (make-syms "a"))
            m-codestr (str "new "
                           (name class)
                           (argslist (map (cvt-to-c args) m-contypes m-funargs)))
            real-funargs (mapv second
                               (remove (fn [x] (given-value (first x) args))
                                       (map vector m-contypes m-funargs)))
            m-funcode (list 'fn real-funargs (obj-encode (name class) m-codestr))
            m-erg (vector (if (seq real-funargs) :fn :new-no-args)
                          m-funcode)]
        m-erg)))

(def call-raw
    (fn [class method args]
      (let [m-m-sub (or (first args) :default)
            m-types (get-ct-malli-types
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
                           (obj-decode (first m-arg-symbols))
                           ")->"
                           (name method)
                           (argslist (map (cvt-to-c args) m-arg-types (rest m-arg-symbols))))
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

  (def vector-to-list
    (fn [fun]
      (fn [x]
        (cond
          (nil? x)
          "nil"
          (map? x)
          (into (hash-map)
                (map (comp vec rest (vector-to-list fun)) x))
          (coll? x)
          (cons 'list (map (vector-to-list fun) x))
          :else (fun x)))))

  (def construct-call
    (fn [sym-fun sym-args macargs c-function m-data]
      (list sym-fun
            (stri macargs)
            ((vector-to-list identity) m-data)
            ((vector-to-list str) m-data)
            sym-args)))

  (def interop-fn
    (fn [macargs c-function m-data]
      (list 'fn [(symbol "&") 'args]
            (construct-call 'checkit 'args macargs c-function m-data)
            (list 'apply (second c-function)
                  (construct-call 'transform 'args macargs c-function m-data)))))

  (def interop-fn-direct
    (fn [macargs c-function m-data]
      (cond
        (= :new-no-args (first c-function))
        (list 'do
              (construct-call 'checkit (list 'list)
                              macargs c-function m-data)
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
                         (get-malli-types [(keyword class) m-types-kw])
                         (get-malli-types [(keyword class)
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
            hack-frt (cons (first frt1) (map (fn [x] (list 'identity x))
                                             (rest frt1)))
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

(defn transform [macargs raw-types str-types args]
  (let [rs (when (not (= "nil" str-types)) (:rtm raw-types))]
    (if rs
      (cons (first args)
            (map (fn [t] (get (second args) (first t)))
                 (rest rs)))
      args)))

(defn not-double? [v]
  (and (not (zero? v)) (zero? (dec (inc v)))))

(defn not-int? [v]
  (not= (floor v) v))

(defn check-value [type v]
  (list
    (cond
      (= type ":double") (if (not-double? v) "-" "+")
      (= type ":int") (if (not-int? v)  "-" "+")
      (= type ":lisc/pos-int") (if (or (not-int? v) (not (pos? v))) "-" "+")
      (= type ":lisc/pos") (if (not (pos? v))  "-" "+")
      (= type ":string") (if-not (string? v) "-" "+")
      :else "?")
    type v))

(defn checkit [macargs raw-types str-types args]
  (let [rs (when (not (= "nil" str-types)) (:rtm raw-types))
        st (when (not (= "nil" str-types)) (get str-types ":rtm"))
        erg (when rs
              (map (fn [r s] (check-value (second s)
                                        (get (second args) (first r))))
                   (rest rs) (rest st)))]
    (when (pos? (count (filter (fn [x] (not= (first x) "+")) erg)))
      (println erg))))

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
(defmacro T [& args] (interop args))

(defmacro > [& args] (_doto args))
(defmacro To [& args] (_doto args))

(defmacro Ts [path cxx rtm]
  (vswap! malli-types
          assoc-in
          (concat path (list :rtm))
          (remove-kw-ns (vec (cons :map rtm))))
  (vswap! malli-types
          assoc-in
          (concat path (list :cxx))
          (remove-kw-ns (vec (cons :cat cxx))))
  nil)

(m-load-types "malli_types.edn" "root_defaults.edn")

(comment

  (m-load-types "malli_types.edn")

  (new-raw 'TF1 [:XR2])
  (new-raw 'TCanvas [])
  (new-raw 'TF1 [:native 'cpp_nslit])
  (def u1 (get-malli-types [:TF1 :SetParameters :default]))

  ;;
  )
