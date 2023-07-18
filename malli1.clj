(defmacro malli-fns []
  (def remove-kw-ns
    (fn [v]
      (mapv (fn [x] (cond
                      (vector? x) (remove-kw-ns x)
                      (qualified-keyword? x) (keyword "lisc" (name x))
                      :else x))
            v)))

  (def maps-to-vector
    (fn [m]
      (mapv (fn [x] (cond
                      (map? x) (maps-to-vector (->> x
                                                    (cons :map)
                                                    (into (vector))))
                      (vector? x) (maps-to-vector x)
                      :else x))
            m)))



  (def vector-to-maps
    (fn [m]
      (cond
        (and (vector? m) (= :map (first m)))
        (into (hash-map) (vector-to-maps (rest m)))
        (coll? m)
        (mapv vector-to-maps m)
        :else
        m)))
  nil)

(malli-fns)

(defmacro huxi []
  (def qq (read-string (slurp "malli1.edn")))
  (def qq1 (remove-kw-ns (maps-to-vector qq)))
  (vector-to-maps [:map [:a 1] [:b 2]])
  (str (vector-to-maps qq1)))

(println (huxi))
