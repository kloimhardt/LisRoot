(defn hu [] 999)

(def types {:TCanvas (list "string" "string" "int" "int" "int" "int")})

;; no eval!!!
(defn add-strs [& s] (new-string (apply concat s)))
(defn int->str [i] (first (str-tok (ntos i) ".")))
;; no eval !!!

(defn core-interpose [sep lst]
  (rest (interleave (repeatedly (count lst) (fn [] sep)) lst)))

(comment

(do
  (def add-strs str)
  (def int->str str))

)

(defn cvt [t v]
  (cond
    (= t "string") (add-strs "\"" v "\"")
    (= t "int") (int->str v)
    :else v))

(defn c_new [kw ty & args]
  (add-strs (cvt (first (get ty kw)) (first args))))

(println (c_new :TCanvas types "N" "B" 0 0 800 600))

(def ar (list "N" "B" 0 0 800 600))
(def d (map cvt (get types :TCanvas) ar))
(def erg (apply add-strs (core-interpose ", " d)))

(println erg)

