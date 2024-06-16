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
    return yamlscript.YAMLScript().load(ys_header + "ys/compile(\"" + code + "\")")

# the following YAMLScript code is correctly translated to Clojure
# but it has lots of necessary backslashes in it

not_minglable ="""
doto:\\n
  newTF1 'pyf2':\ l -1. 1. 2\n
\ SetParameters:\ 5. 2.\n
\ Draw:\
"""

print(to_clojure(ys_header+not_minglable))

# mingle_text() inserts the necessary backslashes for the "yaml_code" below
# but it is not sophisticated enough for the above "not_minglable" case

def mingle_text(t):
    return t.replace(": ", ":\\ ").replace(":\n", ":\\n").replace("\n", "\n\n")

yaml_code = """
native-header: 'ROOT.h'

require cxx: => ROO

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

# YAML-code is data and surely can be shown as JSON structure
print(to_json(yaml_code))

# the mingled YAML-code needs a header to become YAMLScript-code
ys_code = ys_header+mingle_text(yaml_code)

# Clojure code is generated out of YAMLScript-code
clojure_code = to_clojure(ys_code)

# YAMLScript compiles to Clojure code that uses "+_" for arithmetic addition
# Ferret, our Clojure-to-C++ compiler does not like that
# to make amends we add a Clojure definition to the code
ferret_code = "(def +_ +)" + clojure_code

# same for "*_", but in a dirty way
ferret_code = ferret_code.replace("*_", "*")

# we again prefer it clean for the YAMLScript speciality "+++"
ferret_code = "(def +++ identity)" + ferret_code

# write Clojure code to file and shell out to Ferret and Clang compiler
with open("temp.clj", "w") as text_file:
    print(ferret_code, file=text_file)
os.system("java -jar ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
