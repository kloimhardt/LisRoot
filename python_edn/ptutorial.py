import os, yamlscript, edn_format

ys_code = """
!yamlscript/v0

defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()

f =:
  ROOT(new TF1) 'pyf2': l -1. 1. 2

ROOT(SetParameters TF1) f: 5. 2.

c =:
  ROOT(new TCanvas):

ROOT(Draw TF1) f:
ROOT(Print TCanvas) c: 'ptutorial_3.pdf'

newTF1 =: ROOT(new TF1 [string string])
Draw =: ROOT(Draw TF1)

Draw:
  newTF1 'pyg2':: sin(x * 10)

ROOT(Print TCanvas) c: 'ptutorial_4.pdf'

=>: 'end'
"""

def ys_to_clojure(code):
    code = code.replace(": ", ":\\ ").replace("\n", "\\n")
    return yamlscript.YAMLScript().load("!yamlscript/v0\nys/compile(\"" + code + "\")")

def clj_expr_to_ys(exp):
    return "=>: !clj |\n " + exp

def readfile(filename):
    with open(filename, 'r') as file:
        str_code = file.read()
    return str_code

def clj_program_to_ys(str_code):
    edn_code = edn_format.loads("(" + str_code + ")")
    a = map(lambda x: clj_expr_to_ys(edn_format.dumps(x)), edn_code)
    return '\n'.join(a)

clojure_macros = clj_program_to_ys(readfile('lisroot_clojure_functions.clj') + readfile('lisroot_clojure_macros.clj') + '(m-load-types "malli_types.edn" "root_defaults.edn")')

def macroexpand_edn(macro_code_str, expr_edn):
    code_str = edn_format.dumps(expr_edn)
    ys_code = clj_expr_to_ys("(pr-str (macroexpand-1 '" + code_str + "))")
    res_edn_str =yamlscript.YAMLScript().load("!yamlscript/v0\n" + macro_code_str + "\n" + ys_code)
    return edn_format.loads(res_edn_str)

def sym(s):
    return edn_format.edn_lex.Symbol(s)

def kw(s):
    return edn_format.edn_lex.Keyword(s)

def replaceCode(x):
    if isinstance(x, tuple):
        if x == (sym("ROOT"), sym("new"), sym("TCanvas")):
            return edn_format.loads('(fn [& args] ((fn [x] (apply (fn [] "__result = rt::dense_list(obj<string>(\\\"TCanvas\\\"), obj<pointer>(new TCanvas()))") (transform (list "new" "TCanvas" ":empty") (list :cat) (list ":cat") args))) (checkit (list "new" "TCanvas" ":empty") (list :cat) (list ":cat") args)))')
        if x[0] == sym("ROOT"):
            return macroexpand_edn(clojure_macros, (sym("T"), ) + x[1:])
        if x[0] == sym("+++"):
            return replaceCode(x[1])
        if x[0] == sym("+_"):
            return replaceCode((sym("+"), ) + x[1:])
        if x[0] == sym("*_"):
            return replaceCode((sym("*"), ) + x[1:])
        else:
            return tuple(map(replaceCode,x))
    else:
        return x

def expand_clojure(expand_fn, str_code):
    edn_code = edn_format.loads("(" + str_code + ")")
    expanded_edn_code = expand_fn(edn_code)
    return edn_format.dumps(expanded_edn_code)[1:-1]

# in the generated Clojure code, the functions +_, *_ and +++ need replacement
clojure_code = ys_to_clojure(ys_code)
expanded_code = expand_clojure(replaceCode, clojure_code)
ferret_code = '(native-header "ROOT.h")' + readfile('lisroot_ferret_functions.clj') + expanded_code

with open("temp.clj", "w") as text_file:
    print(ferret_code, file=text_file)

os.system("java -jar ../ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
