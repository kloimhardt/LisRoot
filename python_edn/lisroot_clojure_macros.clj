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

(defmacro new [class & args]
  (let [fncode (new-raw class args)]
    (if (= :new-no-args (first fncode))
      (list (second fncode))
      (second fncode))))

(defmacro call [class method & args]
  (second (call-raw class method args)))

(defmacro defnative [head body]
  (list (quote native-declare) (str head "{return " (eval body) ";}")))

(defmacro _ [& args] (interop args))
(defmacro T [& args] (interop args))

(defmacro > [& args] (_doto args))

(defmacro To [& args] (_doto args))

(defmacro Ts [path cxx & [rtm]]
  (if rtm
    (vswap! malli-types assoc-in path
            (hash-map :rtm (remove-kw-ns (vec (cons :map rtm)))
                      :cxx (remove-kw-ns (vec (cons :cat cxx)))))
    (vswap! malli-types assoc-in path
            (remove-kw-ns (vec (cons :cat cxx)))))
  nil)

(defmacro def-ys-plus []
  (list (quote def) (quote +_) (quote +)))

(defmacro def-ys-star []
  (list (quote def) (quote *_) (quote *)))

(m-load-types "malli_types.edn" "root_defaults.edn")
