# LisRoot
Calling into Root, CERN's C++ data analysis framework, from Lisp. The project uses [Ferret](https://ferret-lang.org), the Clojure-syntax to C++ compiler.

Status: alpha, not usable in production.

A thorough introduction can be found in [this Arxiv paper](https://arxiv.org/abs/2312.13295).

## TL;DR Description

The [Python tutorial](https://root.cern/manual/python/#passing-python-callables-to-c)
```
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
```

translates to Ferret (see `translation.clj`)

```
(native-header "ROOT.h")
(require '[cxx :as ROO])

(defn Linear []
  (fn [[x] [d k]]
    (+ d (* x k))))

(def l (Linear))

(def c (ROO/T new TCanvas))

(def f ((ROO/T new TF1) "pyf2" l -1. 1. 2))
((ROO/T SetParameters TF1) f 5. 2.)
((ROO/T Draw TF1) f)
```

Since `(ROO/T Draw TF1)` is a macro call that expands into a lambda-function that does C++ calls, after adding the obvious bindings, the last three lines can be combined to one expression

```
(doto (newTF1 "pyf2" l -1. 1. 2)
  (SetParameters 5. 2.)
  Draw)
```

Adding a Malli-style Schema,

```
(ROO/Ts [:TF1 :Draw :your-hint]
        [:string]
        [[:style ::one-letter]])
```

we can define a fallback enabled "multimethod" function that checks arguments at runtime and accordingly dispatches to different C++ calls,

```
(defn fallbackDraw [f params]
  (when (:mismatch ((ROO/T Draw TF1 :your-hint) f params))
    ((ROO/T Draw TF1) f)))
```

so we can call

```
(fallbackDraw f {:style "P"})
```

as well as

```
(fallbackDraw f {:style "unknown"})
```

## Prerequisites

Install Root https://root.cern.ch

Install Java https://openjdk.org

Download ferret.jar from https://github.com/nakkaya/ferret

## Try it out

Run all scripts with
```
./runall.sh
```

## Types are valid Malli specs

To generate C++ code for accessing ROOT, type information about ROOT classes and methods is necessary. This information is stored as a [Malli](https://github.com/metosin/malli) structure in the file `malli_types.edn`

Although Malli does not exist for Ferret, nevertheless, the conformity of any EDN structure contained in a file can be checked using standard Clojure:

```
clojure -Sdeps '{:deps {metosin/malli {:mvn/version "0.11.0"}}}' -M  mallitypes.clj
```

You need to install https://clojure.org to run this command.
