# LisRoot
Calling into Root, CERN's C++ data analysis framework, from Lisp. The project uses Ferret, the Clojure-syntax to C++ compiler.

Status: alpha, not usable in production.

## Prerequisits

Install Root https://root.cern.ch

Install Java https://openjdk.org

Download ferret.jar from https://github.com/nakkaya/ferret

## Try it out

Run all scripts with
```
./runall.sh
```

## Types are valid Malli specs

To generate C++ code for accessing ROOT, type information about ROOT classes and methods is necessary. This information is stored as a Malli structure.

Malli, the standard tool to spcify data schemes (https://github.com/metosin/malli), does not exist for Ferret. Nevertheless, signatures are defined in a single Malli-conform scheme, as shown here:

```
clojure -Sdeps '{:deps {metosin/malli {:mvn/version "0.11.0"}}}' -M  mallitypes.clj
```

You need to install https://clojure.org to run this command.

