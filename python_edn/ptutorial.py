import os, yamlscript, edn_format

ys_code = """
!yamlscript/v0
native-header: 'ROOT.h'

require cxx: => ROO

defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()

f =:
  ROO/T(new TF1) 'pyf2': l -1. 1. 2

ROO/T(SetParameters TF1) f: 5. 2.

c =:
  ROO/T(make me a Canvas!):

ROO/T(Draw TF1) f:
ROO/T(Print TCanvas) c: 'ptutorial_3.pdf'

newTF1 =: ROO/T(new TF1 [string string])
Draw =: ROO/T(Draw TF1)

Draw:
  newTF1 'pyg2':: sin(x * 10)

ROO/T(Print TCanvas) c: 'ptutorial_4.pdf'

=>: 'end'
"""

def to_clojure(code):
    code = code.replace(": ", ":\\ ").replace("\n", "\\n")
    return yamlscript.YAMLScript().load("!yamlscript/v0\nys/compile(\"" + code + "\")")

clojure_code = to_clojure(ys_code)

edn_code = edn_format.loads("(" + clojure_code + ")")

def sym(s):
    return edn_format.edn_lex.Symbol(s)

def kw(s):
    return edn_format.edn_lex.Keyword(s)

def replaceCode (x):
    if isinstance(x, tuple):
        if x == (sym("ROO/T"), sym("make"), sym("me"), sym("a"), sym("Canvas!")):
            return (sym("ROO/T"), sym("new"), sym("TCanvas"), kw("empty"))
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

ferret_code = edn_format.dumps(modified_edn_code)[1:-1]

with open("temp.clj", "w") as text_file:
    print(ferret_code, file=text_file)

os.system("java -jar ../ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
