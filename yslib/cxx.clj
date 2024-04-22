(require '[cxx :as ROO]) ;; remove this line

(defmacro def-ys-plus []
  (list 'def '+_ '+))

(defmacro def-ys-star []
  (list 'def '*_ '*))
