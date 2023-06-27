# LisRoot
Calling into CERN's Root data analysis framework from Lisp

Install https://root.cern.ch

Download ferret.jar from https://github.com/nakkaya/ferret

```
java -jar ferret.jar -i basic_interop.clj
```

```
clang++ basic_interop.cpp $(root-config --glibs --cflags --libs) -o basic_interop
```

```
./basic_interop
```
