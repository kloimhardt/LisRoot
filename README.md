# LisRoot
Calling into Root, CERN's C++ data analysis framework, from Lisp. The project uses Ferret, the Clojure-syntax to C++ transpiler.

## Prerequisits

Install Root https://root.cern.ch

Install Java https://openjdk.org

Download ferret.jar from https://github.com/nakkaya/ferret

## Creating C++ objects and calling methods

The main Lisp/C++ interop is done via the command "=>".

Create an instance of the Root class "TF1" (i.e. create an object by calling the constructor):

```
(defn n-times-x [n]
  (fn [[x]] (* n x)))

(def f1 ((=> new TF1) "f1" (n-times-x 2) -5.001 5.0 2))
```

Call the method "Draw" of the class to plot the function "n-times-x":

```
((=> Draw TF1) f1)
```

## Calling different class contructors

To generate C++ code, the signatures (i.e. types of parameters) of C++ constructors and methods need to be known at transpile time.

The default signature  of the "TF1" constructor is specified in the file "malli_types.edn".

If another constructor of "TF1" needs to be called, the types can be specified as part of the call:

```
(def f2 ((=> new TF1 [:foo string string int int]) "f2" "sin(x)" -5 5))
```

The signature here is named :foo. This naming allows it to be reused. One can just as well amend the file "malli_types.edn" with new types.

## The types are valid Malli specs

Malli, a standard tool to spcify data schemes (https://github.com/metosin/malli), does not exist for Ferret. Nevertheless, signatures are defined in a single Malli-conform scheme, as shown here:

```
clojure -Sdeps '{:deps {metosin/malli {:mvn/version "0.11.0"}}}' -M  mallitypes.clj
```

You need to install https://clojure.org to run this command.

## Runtime type validation

Types are automatically checked at runtime "Malli-style". This feature will remain a prototype.

## Try it out

```
java -jar ferret.jar -i root_plot.clj
```

```
clang++ root_plot.cpp $(root-config --glibs --cflags --libs) -o root_plot
```

```
./root_plot
```

Run all scripts with
```
./runall.sh
```
