(defn hu [] 999)

(def types {:TCanvas (list "string" "string" "int" "int" "int" "int")})

;; no eval!!!
(defn str [& s] (new-string (apply concat s)))
;; no eval !!!

(defn cvt [t v]
  (cond
    (= t "string") (str "string::to<std::string>(" v ").c_str()")
    :else v))

(defn c_new [kw ty & args]
  (str (cvt (first (get ty kw)) (first args))))

(println (c_new :TCanvas types "Besch"))
