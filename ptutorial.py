import os, ROOT, yamlscript

class Linear:
    def __call__(self, arr, par):
        return par[0] + arr[0]*par[1]

# create a linear function with offset 5, and pitch 2
l = Linear()
f = ROOT.TF1('pyf2', l, -1., 1., 2)
f.SetParameters(5., 2.)

# plot the function
c = ROOT.TCanvas()
f.Draw()
c.Print("ptutorial_1.pdf");

def to_json(t):
    return yamlscript.YAMLScript().load(t)

ys_header = "!yamlscript/v0\n"

def to_clojure(code):
    return yamlscript.YAMLScript().load(ys_header + "ys/compile(\"" + code + "\")").replace("+++", "identity")

# the following YAMLScript code is correctly translated to Clojure
# but it has lots of backslashes in it

not_minglable ="""
doto:\\n
  newTF1 'pyf2':\ l -1. 1. 2\n
\ SetParameters:\ 5. 2.\n
\ Draw:\
"""

print(to_clojure(ys_header+not_minglable))

# mingle_text() inserts the backspaces for the "yaml_code" below
# but is not sophisticated enough for the above "not_minglable" case

def mingle_text(t):
    return t.replace(": ", ":\\ ").replace(":\n", ":\\n").replace("\n", "\n\n")

yaml_code = """
native-header: 'ROOT.h'

require cxx: => ROO

ROO/def-ys-plus:
ROO/def-ys-star:

defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()

f =:
  ROO/T(new TF1) 'pyf2': l -1. 1. 2
ROO/T(SetParameters TF1) f: 5. 2.

c =:
  ROO/T(new TCanvas :empty):
ROO/T(Draw TF1) f:
ROO/T(Print TCanvas) c: 'ptutorial_2.pdf'
"""

print(to_json(yaml_code))

clj_code = to_clojure(ys_header+mingle_text(yaml_code))

with open("temp.clj", "w") as text_file:
    print(clj_code, file=text_file)

os.system("java -jar ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
