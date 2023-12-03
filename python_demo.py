# export PYTHONPATH=/usr/local/Cellar/root/6.26.06_2/lib/root
# python3.10 python_demo.py

import ROOT

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
c.Print("python_demo_1.pdf");

print(l([1.0], [2.0, 5.0]))

class LinearB:
    def __init__(self, k, d):
        self._k = k
        self._d = d
    def __call__(self, arr):
        return self._d + arr[0]*self._k

lb = LinearB(2.0, 5.0)
print(lb([1.0])) #comment => 7.0

lb._d = 4.0
print(lb([1.0])) #comment => 6.0

def LinearC(k, d):
    return lambda arr, par: d + k*arr[0]

print(LinearC(2.0, 5.0)([1.0], [0.0]))

def LinearE():
    return lambda arr, par: par[0] + arr[0]*par[1]

m = LinearE()
g = ROOT.TF1('pyf3', m, -1., 1., 2)
g.SetParameters(6., 3.)
g.Draw()
c.Print("python_demo_2.pdf");

n = LinearC(7., 4.)
h = ROOT.TF1('pyf3', n, -1., 1., 2)
h.Draw()
c.Print("python_demo_3.pdf");
