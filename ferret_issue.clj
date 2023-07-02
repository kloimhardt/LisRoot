
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

(defmacro ji [a]
  (def jii [1 2 3])

  (def ji2 (partition 1 2 jii))

  (def ji3 (remove #(vector? (first %)) ji2))

  (str " " (list ji3)))

(println (ji 3))

(defmacro jj [a]
  (def jj1 (map (fn [v]
                  (let [o (first v)
                        t (second v)]
                    (if (vector? t)
                      (vector o (first t))
                      o)))
                [[1 2] [2 3] [3]]))

  (str " " (list jj1))

  (defn f1 [v] ;; destcucturing des not work
    (let [o (first v)
          t (second v)]
      (if (vector? t)
        (vector o (first t))
        o)))

  (str (f1 [1 2]))

  (defn form-arrays [v]
    (->> v
         (partition-all 2 1)
         (remove #(vector? (first %)))
         (map (fn [v]
               (let [o (first v)
                     t (second v)]
                 (if (vector? t)
                   (vector o (first t))
                   o))))))

  (str (list (form-arrays ji3)))
  )

(println (jj 4))
