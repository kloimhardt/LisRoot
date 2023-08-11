### Calling native code

```
(native-declare "
  double linear(double* arr, double* par) {
    return par[0] + arr[0]*par[1];
  } ")

(ROO/To ((new TF1 :XR2-native linear) -1. 1.)
        (SetParameters 5. 2.)
        Draw)
```

Types in a file. Setting defaults via :XR2

The Draw here is not the fallback-enabled function (would need the {:stype "P"} to work). Is under the Umbrella of ROO/To.

Up to now, everything works generic for any C++ Library, Root was just an example

Ts-Default necessary because Draw is not the function (multimethod) but sent by ROOT/T to C++.

### Functional parameters

```
(defn LinearA [d k]
  (fn [[x]]
    (+ d (* k x))))

(doto ((ROO/T new TF1 :XR2) (LinearA 5 2) -1. 1.)
      (Draw {:style "P"}))

```

Draw is the fallback-enabled function defined above, an option is provided.

Above in we did not specify a scheme for `new`. So the default is taken.

Here, the Draw is the multimethod (not under umbrella of ROO/T). Rule: id not under umrella, it must be known by Lisp.

All types are in malli_types.edn

Data schema for `:XR2` is in textfile (edn-file). Can be read and amended by user. One can and will add new types. It contains edn, the Clojure data format. Is just data that can be read my malli.

Type fiels shared between projects, defuaults are project induvidual.

A single type hint should be sufficient to deduce all types in one expression. Scientific code is usually not very interactive, only script character. A sophisticated type inference system is not needed and results in more expressive code mathematic textbook style.

Lisp has reputation of interpreted. But: surprisingly, Syntax actually helps in generating C++ code.

Here we chose to introduce f. For `Draw` we need to specify TF1, becuase ROO/T does not know about the class, as opposed to ROO/To.

It is also possible to do this in Python:  “It is also possible to keep the parameters as data members of the callable instance".

The difference ist, that in Python one could still change the values of the parameters afterwards (albeit not using ROOT's SetParameters). Changing afterwards in  a Lisp Closure is not possible.

### Milking the type Keyword

When using functions, we can access the runtime information that h is of type "TF1".
All the possible options need to be previsiond in the SetParameters functions, it only dispatches to them at runtime.

Whereas with ROO/T Draw, it is already clear at compile time which version of Draw is called.

Like Python, Ferret does not have static typing. This makes it suitable for scripting purposes.

But there is another feature in Clojure: runtime data-scheme validation.

Nevertheless, to the function SetParameters, a double value is passed on.

In order to compile to C we need to specify the variable types somewhere. In Python this is not necessary, it has cppy and reflection. But is cannot create executables. We have the types in a seperate textfile.
Options are possible
```
((ROO/T Draw TF1 :plot-option) g "P")
```

malli_types.edn

```
[:TF1 [:map
        [:Draw [:map
                [:no-args [:cat]]
                [:plot-option [:cat :string]]]]
        [ ... ]]]
```

Define two options :minimal and :plot-style. Syntax  [:cat] [:map] is malli.
:minimal is set as the default in root-defaults.edn, that is why we did not have to specify :no-args as option. A look at malli ed shows that the default for SetParameters is called :two-doubles.


The schema is malli conform (see mallitypes.clj) although we do not have malli at our disposal.


Usually, the type is directly next to the variable. We deliberately do not do this, so code can be amendet by anather person. So on scientist models the function "Linear", another one specifys the types uses it within ROOT.

In Clojure we use this Strategy for runtime checks with malli, but not for interop. Here we combine the two.

We introduce bless. Programmatically it basically does nothing, it is only there to give a place to state the name of the class to be picketd up by ROO/To and handed down to `SetParameters` and `Draw`. It is basically a type hint for `ROO/To` like `TF1` in Clojure.

We get a wariing that -2 in not positive.

This step hes no parallel in Python. And indeed, this specification is typically not done in the source file, but in a dedicated schema file that is shared between projects.

Whereas the runtime scheme gives the parameters their names and specifies the data scheme that pertains to the calculation, the last line is a technical specification necessary for the transpilation process to C++.

Once could also set it with `ROO/Ts-default [:TF1 :SetParameters :line]`. The scheme is just a data structure that can be manipulated at will with standard Clojure methods. The default is already the [:double :double].

The idea is to check the parameters  for conformity to a schema and transform it if necessary before the C- function is called. Of course we do not have malli style capabilities, needed to implement own checks.

Structure is used to do two things: 1 cheks 2)) generate C++i (no reflection). Checks are ony done if transformation scheme is available.

### State vs Staless
In Python it is common to create an object f, do something else and change the object f afterwards again. f has mutable state. The anonymous function does nt have mutable state.

It is interesting to note that the variable `l` is created exactly for the purpose of being changed afterwards. Although in fact it actually isn't. The overall pattern is to create a class and instanciate an object. In Lisp the main pattern is to define (higher order) functions. Concepts more familiar to the scientist. In lisp one avoids introducing the Class-Object dichotomy as much as possible. For Physicists scripting is shouldn't be necessary at all.

We observe that the mathematical notation in typical physics textbooks does not include mutation of mathematical objects. Those are only defined once. Thus we argue that the functional style is more suited to computational physics.

Now comes a central feature of the object oriented paradigm: objects are created to be mutated afterwards.

The Object-Function "l" is fed as argument into ROOTs own function representing class, TF1, thus constructiong the object named "f".  This is very typical for OO style programming: creating objects using a set of parameters and then change the state of objects in a next step to manipulate their behaviour.

Functional programmers try to immediately make all possible changes to an object when it is created. This means that the Canvas allocates memory earlier than needed, becuase it is only until the "Draw" that it is needed. Use as few of those single letter variables as possible.

OO creates objects just before they are needed in the procedure of the step-by-step computation. The result is a flow of objects which are created and changed as needed by the processor.

fist make lots of one letter variables and get rid of them. Use Clojure not Ferret directly to write code.

Do not introduce letters and use as little parantheses as possible.

ROO/To is Clojure's doto.

This also means that sometimes objects are created new instead of using already existing ones, all at the cost of runtime and memory. Only when speed is needed is it given precedence.

`(ROO/T SetParameters TF1)` results in a simple Lisp function. A function whose Lisp code is generated before transpilation to C. The code is essentially `(fn [a_0] "pointer::to_pointer<TF1>(a_0)->Draw()")`. One could write `(def draw (ROO/T Draw TF1)) (draw f)`, the `draw` is an ordinary ferret function like any other.

## Cleaning up

## Recapitulation

## Coda
## Files

`malli-types.edn`: contains types
`toot-defaults.edn`: defaults
`mallitypes.clj`: test malli.edn for conformity
### Execution

```
java -jar ferret.jar -i foo.clj
clang++ foo.cpp $(root-config --glibs --cflags --libs) -o foo
```

 The first line transfiles the source file `foo.clj` to C++. The second line, being independent from Ferret, is the standard call to compile code with the ROOT library.

Ferret is a Java program and distributed as one single .jar file. So besides Java, nothing needs to be installed or buildt on the local machine. The C++ code generated is self contained, it does not import anything in addition to the libraries imported by the user (which of course is ROOT in this case).

## Introduction

> Object Oriented Software design is an art one should not touch unless absolutely needed.

Heraclitus of Ephesus

> A working scientist spends his time crafting experiments to question the existing objects of nature … Not instantiating new hierarchies of classes in his software.

Galileo Galilei in conversation with Vladimir Ilyich Lenin

The authors have created a functional scripting interface to a mature data analysis framework used by high energy physics. The interface gives users a direct, light-weight handle on the framework only using a human readable text-file to duplicate the function signatures of the underlying number-crunching code. The authors also discuss design issues and the advantages of the functional programming paradigm.

Object-oriented programming is the mainstream in almost all computing fields. Although functional programming is gaining momentum, in computational physics its use has been quite modest until recently.

One reason for this lag is the perceived lack of self efficacy of the physics student when not using a mainstream language. Another reason is the dominant focus on speed and a common disregard for succinct notation of underlying theoretical ideas.

This situation is gradually changing as performant implementations of functional programming languages become available [1-3].

```
1 https://cisco.github.io/ChezScheme/
2 https://opengoal.dev
3 https://jank-lang.org
```

This article describes how we created an interface to a mature data analysis framework used by high energy physics. Other researchers have created OO interfaces in the same context [1] but our approach differs because we don’t create sophisticated bridge software. Instead, we use Lisp's compile-time feature to directly generate C++ code where a seperate textfile defines the necessary function signatures as a human readable data-schema. An approach that shows how to interface to a host language using type hints intermingled in code appears elsewhere [2]. Also the practical use of data schemas have been shown [3]. Both our interface and its underlying code are available free under the MIT license.

```
1 https://root.cern/manual/python/ https://root-forum.cern.ch/t/cling-and-pyroot-python/18059
2 https://clojure.org/reference/java_interop#typehints
3 https://github.com/metosin/malli
```
### ROOT
The ROOT code was developed at CERN, the international research center for particle physics. The code’s applications cover a broad spectrum ranging from function minimization to plotting. The ROOT code originated in the 1990s and is written in C++. Recent modernization brings in a Python interface.
