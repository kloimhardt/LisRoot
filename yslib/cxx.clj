(require '[cxx :as ROO])

(defmacro def-ys-plus []
  (list 'def '+_ '+))

(defmacro def-ys-star []
  (list 'def '*_ '*))

(defmacro def-ys-tripleplus []
  (list 'def '+++ 'identity))
