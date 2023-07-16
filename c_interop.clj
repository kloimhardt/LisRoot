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
          (keyword? t) (c-lambda v (let [ts (get-in root-types [:Types :Functions t])]
                                     (cons (last ts) (drop-last 2 ts))))
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
            lasttwo (take-last 2 funtypes)
            return-type (if (= (str (first lasttwo)) "->")
                          (second lasttwo) (symbol "void"))
            arg-types (if (= return-type (symbol "void"))
                        funtypes
                        (drop-last 2 funtypes))
            arg-symbols (->> arg-types count inc (make-syms "a"))
            codestr (str "pointer::to_pointer<"
                         (name class)
                         ">("
                         (first arg-symbols)
                         ")->"
                         (name method)
                         (argslist (map (cvt-to-c native-string)
                                        arg-types
                                        (rest arg-symbols))))]

        (list 'fn arg-symbols
              (if (= return-type (symbol "void"))
                codestr
                (wrap-result
                  return-type
                  codestr))))))

  (def bakeclass
    (fn [class s args]
      (let [c-sub (or s :A)
            types (get-in root-types [:Types :Classes class c-sub])
            code (new-raw class (cons s args))]
        (if (seq types) code (list code)))))

  (def bakemethod
    (fn [method class t args]
      (let [m-sub (or t :A)]
        (call-raw class method (cons t args)))))

  (def bake
    (fn [args]
      (let [classes (get-in root-types [:Types :Classes])
            method (first args)
            class (second args)
            class? (get classes class)
            types (first (nnext args))
            types-kw (if (vector? types) (first types) types)
            r (next (nnext args))]
        (do
          (cond
            (< (count args) 2)
            (println "Transpile refused: bake needs at least two args")
            (and (vector? types) (= (symbol "new") method))
            (add-type-raw (list :Types :Classes class) types)
            (and (vector? types) class?)
            (add-type-raw (list :Types :Classes class method) types))
          (if (= (symbol "new") method)
            (bakeclass class types-kw r)
            (bakemethod method class types-kw r))))))

  nil)

(class-fns)

(defmacro new [class & args]
  (new-raw class args))

(defmacro call [class method & args]
  (call-raw class method args))

(defmacro defnative [head body]
  (list 'native-declare (str head "{return " (eval body) ";}")))
