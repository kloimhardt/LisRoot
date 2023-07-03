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

  nil)

(type-fns)

(defmacro set-types [t]
  (alter-var-root (var root-types) (constantly (make-types t)))
  nil)

(defmacro load-types [filename]
  (alter-var-root (var root-types)
                  (->> filename slurp read-string make-types constantly))
  nil)

(defmacro add-type [path t]
  (alter-var-root (var root-types)
                  assoc-in
                  (concat (list :Types) path (list (first t)))
                  (rest t))
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
        (vector? t) (str "obj<array_seq<"
                         (first t)
                         ", number>>("
                         v
                         ", size_t("
                         (second t)

                         "))"))))
  (def argslist
    (fn [strs]
      (str "(" (apply str (interpose ", " strs)) ")")))

  (def cvts-to-c
    (fn [t v]
      (cond
        (= t 'string) (str "string::to<std::string>(" v ").c_str()")
        (= t 'int) (str "number::to<std::int32_t>(" v ")")
        (= t 'double) (str "number::to<double>(" v ")"))))

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
    (fn [t v]
      (cond
        (keyword? t) (c-lambda v (get-in root-types [:Types :Functions t]))
        :else (cvts-to-c t v))))

  (def wrap-result
    (fn [t s]
      (str "__result = " (cvt-from-c t s))))

  nil)

(class-fns)

(defmacro new [class & args]
  (let [c-sub (or (first args) :A)
        contypes (get-in root-types [:Types :Classes class c-sub])
        funargs (->> contypes count (make-syms "a"))
        codestr (str "new "
                     (name class)
                     (argslist (map cvt-to-c contypes funargs)))]
    (list 'fn funargs (wrap-result 'pointer codestr))))

(defmacro call [class method & args]
  (let [m-sub (or (first args) :A)
        funtypes (get-in root-types [:Types :Classes class method m-sub])
        funargs (->> funtypes count (make-syms "a"))
        codestr (str "pointer::to_pointer<"
                     (name class)
                     ">("
                     (first funargs)
                     ")->"
                     (name method)
                     (argslist (map cvt-to-c (rest funtypes) (rest funargs))))]
    (list 'fn funargs
          (if (= (first funtypes) 'null)
            codestr
            (wrap-result (first funtypes) codestr)))))

