# LisRoot
Calling into CERN's Root data analysis framework from Lisp

Install Root https://root.cern.ch

Install Java https://openjdk.org

Download ferret.jar from https://github.com/nakkaya/ferret

```
java -jar ferret.jar -i root_plot.clj
```

```
clang++ root_plot.cpp $(root-config --glibs --cflags --libs) -o root_plot
```

```
./root_plot
```

Install https://clojure.org

```
clojure -Sdeps '{:deps {metosin/malli {:mvn/version "0.11.0"}}}' -M  mallitypes.clj
```
