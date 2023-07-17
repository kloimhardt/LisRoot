(defmacro hui []
  (def a (read-string (slurp "malli.edn")))

  (def remove-kw-ns
    (fn [v]
      (mapv (fn [x] (cond
                      (vector? x) (remove-kw-ns x)
                      (keyword? x) (keyword (name x))
                      :else x))
            v)))

  (def malli-to-map
    (fn [v]
      (reduce (fn [acc pair]
                (if (vector? (second pair))
                  (if (= :multi (first (second pair)))
                    (if (get-in acc (vector :Types :Classes (get-in (second pair) (vector 2 1 1 1 1))))
                      (reduce (fn [cacc v]
                                          (assoc-in cacc (vector :Types :Classes (get-in v (vector 1 1 1 1)) (first pair) (first v)) (second v)))
                                        acc (nnext (second pair)))
                      (assoc-in acc (vector :Types :Classes (first pair)) (into (hash-map) (nnext (second pair)))))
                    (assoc-in acc (vector :Types :Functions (first pair)) 1))
                  (assoc-in acc (vector :Types :Functions (first pair)) 1))
                )
              (hash-map) (partition 2 (remove-kw-ns v)))))

  nil)

(hui)

(defmacro hax []
   (str (malli-to-map a)))

(println (hax))
