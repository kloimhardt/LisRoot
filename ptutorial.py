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


yaml_text = """
native-header: 'ROOT.h'

require cxx: => ROO

ROO/def-ys-plus:
ROO/def-ys-star:

defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()

c =:
  ROO/T(new TCanvas :empty):

f =:
  ROO/T(new TF1) 'pyf2': l -1. 1. 2

ROO/T(SetParameters TF1) f: 5. 2.
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

data = yamlscript.YAMLScript().load(yaml_text)
print(data)

with open("temp.ys", "w") as text_file:
    print("!yamlscript/v0\n" + yaml_text, file=text_file)

os.system("ys -c temp.ys >temp.clj && java -jar ferret.jar -i temp.clj && clang++ temp.cpp $(root-config --glibs --cflags --libs) -o temp && ./temp")
