# YAMLScript joins PyROOT to generate C++

YAML, the industry standard for config files with support in over 40 languages, has recently been extended by YAMLScript [^1].
In combination with the LisRoot library described in this forum [^2], YAMLScript can be compiled to C++ and used to access ROOT.
The creator of YAMLScript has made a Docker setup for people to try out ROOT + YAMLScript with minimal installation [^3].

As shown in the LisRoot example [^4], the following Python code:

```
class Linear:
    def __call__(self, arr, par):
        return par[0] + arr[0]*par[1]

l = Linear()

f = ROOT.TF1('pyf2', l, -1., 1., 2)
```

translates to YAMLScript as:

```
defn Linear():
  fn([x] [d k]): d + (k * x)

l =: Linear()

f =:
  ROO/T(new TF1) 'pyf2': l -1. 1. 2
```

YAMLScript consists of expressions that are valid YAML data structures, allowing code manipulations for symbolic algebra or indeed transparent C++ interop.

[^1]: https://yamlscript.org  
[^2]: https://root-forum.cern.ch/t/developed-a-prototype-to-access-root-from-lisp/57633  
[^3]: https://github.com/kloimhardt/LisRoot?tab=readme-ov-file#try-it-easier-with-docker-and-make  
[^4]: https://github.com/kloimhardt/LisRoot/blob/main/ptutorial.py
