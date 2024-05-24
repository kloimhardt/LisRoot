# YAMLScript as new Python for Data Science
## YAML is a data format
YAML is a data format. It is used in math to store algorithms, in software engineering it is used to store program code, in meteorology to store weather forecasts. In math they call it formula, in computing they call it syntax, in meteorology they call it, well, data. To us it is all the same: YAML.

An example: a car driving at 20mph for 2 hours will drive 40 miles. This is because `40 = 20 * 2`, and for this calculation the official formula is `s = v * t`.

This formula can be stored, if one wishes, in YAML as `defn s(v, t): v * t`. Believe it or not, this is valid YAML. As a proof, let YAMLScript, the YAML compiler, transform that formula into the JSON data format: `{"defn s(v, t)":"v * t"}`. Call it formula or syntax or data, up to this point it is all the same to us.

## Execute the data
YAMLScript also delivers a data format called Clojure, our formula reads `(defn s [v t] (*_ v t))` and in Clojure lingo that format is called an S-Expression. The cool thing is that Clojure can be executed. This means that the following YAML results in the number `40`:

```
defn s(v, t): v * t
say: s(20, 2)
```

For the record, in JSON the above program reads `{"defn s(v, t)":"v * t", "say":"s(20, 2)"}`.

Writing programs in a data format and calling that expressions is no news in science. The very popular Mathematica(TM) package does that already for decades to extreme success. In that context, data transformation is called expression manipulation or "symbolic calculation". And as a reminiscence to a traditional concept, snippets of YAML data are sometimes also called YeS-Expressions.

## Syntax

In the CERN tutorial Python

```
class Linear:
    def __call__(self, arr, par):
        return par[0] + arr[0]*par[1]

l = Linear()
```

It involves class etc. is called OO programming.

YAML

```
defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()
```

Only functions.

## Macros, the magic sauce for C++ interop

C++ interop is a thorny subject. Many languages have a good way to interop with the ANSI-C language. But when it comes to C++, it gets hairy.

Macros to the rescue. Macros are just functions that take data and return data that then in turn is executed. In a way, Macros are used for what was above called expression manipulation. The same concept all over again. In Macros, data is transformed from and to S-Expressions.

For our interop, we manually encode the signature of the classes, functions etc. of some C++ library as a, you might have guessed, data structure. This allows us, in a first step and via Macros, to generate the necessary interop functions at compile time. No intermediate or temporary files, no special compile step. Just the signature-data and Macros.

But the C++ signature-data is not just used at compile time. The very same data is used at runtime to perform a trick called polymorphic dispatch which is mandatory when calling into C++, a feature many interop schemes grapple with.

## Immutability left out

To be sure, expressions or formulas have no "side effects", they are "pure" and "stateless", meaning that they do not change any data in place. Indeed expressions are data which is executed. They (possibly reading other data) yield new data. This is very sciency and mathy and the Clojure language is a master at all this. The upshot is that when some general program, even an interactive web page, is written as much as possible in this pure "mathy" style, it is easier to phathom what that software is doing when going wrong.

Our formula is data. Distinct from this being-data-in-itself property is that it is "pure". This means that while a general program changes data in place, a formula does not. A formula always only yields new data.

this mathy style is also subsumed under the lose term functional programming. Functions that are formulas, meaning that they do not change data in place, that they do not have "side effects", are sometimes also called pure functions. Everyone uses those terms in slightly different meanings. Being it formula, expression or pure function, it is all YAML.

Of course, a general program cannot be pure in that sense. Indeed, the subjects called "immutable data structures" and "concurrent mutable state" fill whole books. When adhering to the "pure" approach, the aim in practice is to change data in one place only, to isolate the mutable state, to maintain a central database.

One might ask why this "immutability" was not already given to us in Python and we needed to wait for YAMLScript? Reason is that adhering to pure style can take up more memory and is sometimes slower, so it cannot be used everywhere to great effect. But in science immutability should be the default.

