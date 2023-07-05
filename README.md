# LisRoot
Calling into CERN's Root data analysis framework from Lisp

Install https://root.cern.ch

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
