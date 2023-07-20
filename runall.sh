#!/bin/bash
for file in root_plot python_comparison cpp_comparison cpp_native; do

    java -jar ferret.jar -i $file.clj
    clang++ $file.cpp $(root-config --glibs --cflags --libs) -o $file
    ./$file

done
