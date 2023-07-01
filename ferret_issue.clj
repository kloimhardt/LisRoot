
;; (println ((fn [i f] "__result = run(f, i)") 2 inc)) ;; nono

(println ((fn [i f] "__result = run(f, i)") 2 (fn [i] (inc i))))

(defn call-func [f i]
  "__result = run(f, i)")

(defmacro make-call-plus [n]
  (list 'fn '[i]
        (str "__result = obj<number>(number::to<std::int32_t>(i) + " n ")")))

(defmacro make-call-func [n]
  (list 'fn '[f]
        (str "__result = run(f, obj<number>(" n "))")))

(println (call-func inc 2)) ;;=> 3
(println ((make-call-plus 1) 2)) ;;=> 3
(println ((make-call-func 2) (fn [i] (inc i)))) ;; => 3

;; (println ((make-call-func 2) inc)) ;;does not work

(defmacro make-call-funky [n]
  (list 'do
        (list 'defn 'ha '[f]
              (str "__result = run(f, obj<number>(" n "))"))
        'ha))

;; (println ((make-call-funky 2) inc)) ;; does not work
