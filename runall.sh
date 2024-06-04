#!/bin/bash

python3 python_demo.py
# python3.10 python_demo.py

ys -c ytranslation.yaml >ytranslation.clj

for file in root_plot python_comparison cpp_comparison cpp_native translation ytranslation; do
# for file in translation; do
    rm $file.cpp
    rm $file
    java -jar ferret.jar -i $file.clj
    clang++ $file.cpp $(root-config --glibs --cflags --libs) -o $file
    ./$file
done

clojure -Sdeps '{:deps {metosin/malli {:mvn/version "0.11.0"}}}' -M  mallitypes.clj

root 'yamltoc.C("ccode.yaml")' -q

root GuiExample.C
