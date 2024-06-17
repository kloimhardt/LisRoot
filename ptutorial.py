import os, ROOT, yamlscript

# example taken from https://root.cern/manual/python

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

# YAMLScript code for the same example

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
  ROO/T(new TCanvas :empty):

ROO/T(Draw TF1) f:
ROO/T(Print TCanvas) c: 'ptutorial_2.pdf'

newTF1 =: ROO/T(new TF1)
SetParameters =: ROO/T(SetParameters TF1)
Draw =: ROO/T(Draw TF1)

doto:
  newTF1 'pyf2': l -1. 1. 2
  SetParameters: 5. 2.
  Draw:

ROO/T(Print TCanvas) c: 'ptutorial_3.pdf'

=>: 'end'
"""

def to_clojure(code):
    code = code.replace(": ", ":\\ ").replace("\n", "\\n")
    return yamlscript.YAMLScript().load("!yamlscript/v0\nys/compile(\"" + code + "\")")

# Clojure code is generated out of YAMLScript-code
clojure_code = to_clojure(ys_code)

# YAMLScript compiles to Clojure code that uses "+_" for arithmetic addition
# Ferret, our Clojure-to-C++ compiler does not like that, it wants simple "+"
# to make amends we add the following specific Clojure definition to the code
ferret_code = "(def +_ +)" + clojure_code

# to make amends for the "*_", we use a more dirty way, simply because we can
ferret_code = ferret_code.replace("*_", "*")

# The YAMLScript speciality "+++" is also unknown to Ferret
# and we remove it via the golden path of Lisp: a Macro
ferret_code = "(defmacro +++ [e] e)" + ferret_code

# write Clojure code to file and shell out to Ferret and Clang compiler
with open("temp.clj", "w") as text_file:
    print(ferret_code, file=text_file)
os.system("java -jar ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
