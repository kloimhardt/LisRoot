import os, ROOT

# The following example is taken from https://root.cern/manual/python
# it is translated to YAMLScript and executed using the ys command line tool
# PRs are very welcome, also for the short text to be found in
# https://github.com/kloimhardt/LisRoot/blob/main/paper/access_root_with_ys.md

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

g = ROOT.TF1('pyg2', 'sin(x * 10)')
g.Draw()
c.Print("ptutorial_2.pdf");

# YAMLScript code for the same example

ys_code = """
!yamlscript/v0
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
ROO/T(Print TCanvas) c: 'ptutorial_3.pdf'

newTF1 =: ROO/T(new TF1 [string string])
Draw =: ROO/T(Draw TF1)

Draw:
  newTF1 'pyg2':: sin(x * 10)

ROO/T(Print TCanvas) c: 'ptutorial_4.pdf'

=>: 'end'
"""

# the YAMLScript code is written to file and executed using ys, ferret and clang

with open("temp.ys", "w") as text_file:
    print(ys_code, file=text_file)

os.system("ys -c temp.ys >temp.clj && java -jar ../ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
