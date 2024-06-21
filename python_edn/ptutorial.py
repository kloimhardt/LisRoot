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
  ROOT(make me a Canvas!):

ROOT(Draw TF1) f:
ROOT(Print TCanvas) c: 'ptutorial_3.pdf'

newTF1 =: ROOT(new TF1 [string string])
Draw =: ROOT(Draw TF1)

Draw:
  newTF1 'pyg2':: sin(x * 10)

ROOT(Print TCanvas) c: 'ptutorial_4.pdf'

=>: 'end'
"""

def to_clojure(code):
    code = code.replace(": ", ":\\ ").replace("\n", "\\n")
    return yamlscript.YAMLScript().load("!yamlscript/v0\nys/compile(\"" + code + "\")")

def wrap_clojure_exp(exp):
    return "=>: !clj |\n " + exp

def macroexpand_clojure_exp(macro_code, code):
    a = wrap_clojure_exp("(pr-str (macroexpand-1 '" + code + "))")
    return yamlscript.YAMLScript().load("!yamlscript/v0\n" + macro_code + "\n" + a)

def macroexpand_edn(macro_code_str, code_edn):
    return edn_format.loads(macroexpand_clojure_exp(macro_code_str, edn_format.dumps(code_edn)))

def readfile(filename):
    with open(filename, 'r') as file:
        str_code = file.read()
    return str_code

def compose2(f, g):
    return lambda *a, **kw: f(g(*a, **kw))

def yaml_clojure_file(filename):
    str_code = readfile(filename)
    edn_code = edn_format.loads("(" + str_code + ")")
    a = map(compose2(wrap_clojure_exp, edn_format.dumps), edn_code)
    return '\n'.join(a)

clojure_code = to_clojure(ys_code)

edn_code = edn_format.loads("(" + clojure_code + ")")

def sym(s):
    return edn_format.edn_lex.Symbol(s)

def kw(s):
    return edn_format.edn_lex.Keyword(s)

clojure_macros = yaml_clojure_file('lisroot_clojure_functions.clj') +"\n" + yaml_clojure_file('lisroot_clojure_macros.clj')

def replaceCode (x):
    if isinstance(x, tuple):
        if x == (sym("ROOT"), sym("make"), sym("me"), sym("a"), sym("Canvas!")):
            return replaceCode((sym("ROOT"), sym("new"), sym("TCanvas"), kw("empty")))
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

# in the generated Clojure code, the functions +_, *_ and +++ need replacement
modified_edn_code = replaceCode(edn_code)

ferret_code = '(native-header "ROOT.h")' + readfile('lisroot_ferret_functions.clj') + edn_format.dumps(modified_edn_code)[1:-1]

with open("temp.clj", "w") as text_file:
    print(ferret_code, file=text_file)

os.system("java -jar ../ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
