(defmacro type-fns []
  (def form-arrays
    (fn [v]
      (->> v
           (partition-all 2 1)
           (remove #(vector? (first %)))
           (map (fn [v]
                  (let [o (first v)
                        t (second v)]
                    (if (vector? t)
                      (vector o (first t))
                      o)))))))

  (def make-types-1
    (fn [v]
      (if (every? vector? (rest v))
        [(first v) (into (hash-map) (map make-types-1 (rest v)))]
        [(first v) (form-arrays (rest v))])))

  (def make-types
    (fn [v]
      (apply hash-map (make-types-1 v))))

  (def root-types (hash-map))

  (def set-types-raw
    (fn [t]
      (alter-var-root (var root-types) (constantly (make-types t)))))

  (def add-type-raw
    (fn [path t]
      (alter-var-root (var root-types)
                      assoc-in
                      (concat path (list (first t)))
                      (rest t))))
  nil)

(type-fns)

(defmacro load-types [filename]
  (set-types-raw (read-string (slurp filename)))
  nil)

(defmacro add-type [path t]
  (add-type-raw (concat (list :Types) path) t)
  nil)

(defmacro add-signature [path t]
  (add-type-raw (concat (list :Types :Classes) path) t)
  nil)

(defmacro class-fns []
  (def make-syms
    (fn [s n]
      (mapv #(symbol (str s "_" %)) (range n))))

  (def cvt-from-c
    (fn [t v]
      (cond
        (= t 'string) (str "obj<string>(" v ")")
        (= t 'pointer) (str "obj<pointer>(" v ")")
        (= t 'double) (str "obj<number>(" v ")")
        (vector? t) (str "obj<array_seq<"
                         (first t)
                         ", number>>("
                         v
                         ", size_t("
                         (second t)

                         "))")
        :else v)))

  (def argslist
    (fn [strs]
      (str "(" (apply str (interpose ", " strs)) ")")))

  (def cvts-to-c
    (fn [t v]
      (cond
        (= t 'string) (str "string::to<std::string>(" v ").c_str()")
        (= t 'int) (str "number::to<std::int32_t>(" v ")")
        (= t 'double) (str "number::to<double>(" v ")")
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

  (def c-lambda
    (fn [varname signature]
      (let [funargs (make-syms "b" (dec (count signature)))
            argstypes (map (fn [e] (if (vector? e) (str (first e) "*") e))
                           (rest signature))
            combined (map (fn [t v] (str t " " v)) argstypes funargs)]
        (str "[" varname "] " (argslist combined) " -> " (first signature)
             " {" (c-lambdabody varname signature) "}"))))

  (def cvt-to-c
    (fn [native-string]
      (fn [t v]
        (cond
          (= :native-string t) native-string
          (keyword? t) (c-lambda v (get-in root-types [:Types :Functions t]))
          :else (cvts-to-c t v)))))

  (def wrap-result
    (fn [t s]
      (str "__result = " (cvt-from-c t s))))

  (def new-raw
    (fn [class args]
      (let [c-sub (or (first args) :A)
            native-string (second args)
            contypes (get-in root-types [:Types :Classes class c-sub])
            funargs (->> contypes count (make-syms "a"))
            codestr (str "new "
                         (name class)
                         (argslist (map (cvt-to-c native-string) contypes funargs)))]
        (list 'fn funargs (wrap-result 'pointer codestr)))))

  (def call-raw
    (fn [class method args]
      (let [m-sub (or (first args) :A)
            native-string (second args)
            funtypes (get-in root-types [:Types :Classes class method m-sub])
            funargs (->> funtypes count (make-syms "a"))
            codestr (str "pointer::to_pointer<"
                         (name class)
                         ">("
                         (first funargs)
                         ")->"
                         (name method)
                         (argslist (map (cvt-to-c native-string) (rest funtypes) (rest funargs))))]
        (list 'fn funargs
              (if (= (first funtypes) 'null)
                codestr
                (wrap-result (first funtypes) codestr))))))

  (def bake
    (fn [args classname]
      (let [c-sub (or (first args) :A)
            class (symbol classname)
            types (get-in root-types [:Types :Classes class c-sub])]
        (cond
          (and (seq types) (not (map? types)))
          (new-raw (symbol classname) args)
          (and (not (seq types)) (map? types))
          (list (new-raw (symbol classname) args))
          :else
          (call-raw (symbol classname) (first args) (rest args))))))

  nil)

(comment
  (load-types "root_types.edn")

  (defn bakeclass [class s args]
    (let [c-sub (or s :A)
          types (get-in root-types [:Types :Classes class c-sub])
          code (new-raw class (cons s args))]
      (if (seq types) code (list code))))

  (defn bakemethod [method class t args]
    (let [m-sub (or t :A)]
      (call-raw class method (cons t args))))

(defn bake2 [args]
  (let [classes (get-in root-types [:Types :Classes])
        f (first args)
        s (second args)
        t (first (nnext args))]
    (do
      (cond
        (vector? s)
        (add-type-raw (list :Types :Classes f) s)
        (and (get classes s) (vector? t))
        (add-type-raw (list :Types :Classes s f) t))
      (cond
        (get classes f)
        (bakeclass f (if (vector? s) (first s) s) (nnext args))
        (get classes s)
        (bakemethod f s (if (vector? t) (first t) t) (next (nnext args)))
        :else nil))))

(defn g [& args] (bake2 args))

(g 'TF1)
(g 'TCanvas)
(g 'TCanvas [:E 'double])
(g 'TCanvas [:F])
(g 'TCanvas :F)

(g 'SetParameters 'TF1)
;;
  )

(class-fns)

(defmacro new [class & args]
  (new-raw class args))

(defmacro call [class method & args]
  (call-raw class method args))

(defmacro defnative [head body]
  (list 'native-declare (str head "{return " (eval body) ";}")))
