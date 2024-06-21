(defn transform [macargs raw-types str-types args]
  (let [rs (when (and (not (= "nil" str-types))
                      (get str-types ":rtm"))
             (get raw-types ":rtm"))]
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
      (= type ":lisc/one-letter") (if-not (and (string? v) (= 1 (count v)))  "-" "+")
      (= type ":string") (if-not (string? v) "-" "+")
      :else "?")
    type v))

(defn checkit [macargs raw-types str-types args]
  (let [st (when (not (= "nil" str-types)) (get str-types ":rtm"))
        rs (when st (get raw-types ":rtm"))
        method (first macargs)
        dict (second args)
        erg (when rs
              (cond
                (and (not= "new" method) (not= (second macargs) (first (first args))))
                (list "-" "wrong class")
                (not dict)
                (list "-" "no second arg")
                :else
                (map (fn [r s]
                       (let [type (second s)
                             kw (first r)]
                         (check-value type (get dict kw))))
                     (rest rs) (rest st))))]
    (when (pos? (count (filter (fn [x] (not= (first x) "+")) erg)))
      {:mismatch erg})))
