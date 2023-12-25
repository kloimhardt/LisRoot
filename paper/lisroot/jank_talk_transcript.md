# Talk about the jank compiler given by Jeaye Wilkerson at the [Compiler Research monthly meeting of Oct 22](https://compiler-research.org/meetings/#caas_06Oct2022) (excerpt transcript)

## [00:00 - 08:30 Introduction]

jank is a Clojure dialect. Clojure is a practical Lisp for the JVM but also runs on JavaScript, CLR and Beam, so it latches on to various dynamic hosts. I'm trying to bring all of what I like about it to the native world [i.e. C++]. One of the big things around that is interactive programming and REPLs, which is where Cling [CERN's C++ interpreter] comes in.

In broad strokes I'd like to show an interactive programming demo of Clojure. This is because when we talk about interactive C++, there is a disconnect between what Cling programmers think what a Repl [Read-Eval-Print-Loop] is and what Clojure and Lisp programmers mean when they talk about Repl. So I'd like to show what things are like on the Clojure end.

Then I'm going to dive a bit into Clojure's interactive compilation model which, coming from my background of systems programming, blew my mind over and over again when I was coming up with how to actually do this in jank.

I will cover a lot of Clojure live demos here because jank is at this stage less developed and I want to show where I'm trying to get and only then talk about where I am and what's remaining and how this would apply to a native world like e.g. Vulcan 3D graphics games [or other performance critical software coded under time constraints].

I'd like to talk about what a Repl means to me and what does a Repl mean to you. On a surface level, I do not need to tell Cling and data-science people what a Repl actually is. But there are different ways that it manifests.

Mainstream (i.e. Node or Ruby) programmers, when they think about Repls, they think: "ok, I can start typing on the prompt and get some results". Another group thinks more about Jupyter notebooks. But that world I have seen in videos, this kind of interactive programming is entirely outside of what I work with in games.

In game tooling, when I'm trying to get interactive programming it's because I want to start with an empty process and then build it up into a game without needing to stop and recompile my game and run it again to do some more testing. I want to be able to do that interactively.

I have seen some uses of this with regard to music creation, but that didn't quite achieve I think what the Lisp world has been doing for a while. So let's jump into a little bit more of what I mean with a demo.

[06:12 starts a Clojure session in the terminal] On the Clojure Repl, we can verify our sanity and that's great [types (+ 2 3) and gets 5, i.e. mainstream expectations are met]. But there is something else. One can notice here that this n-Repl server is started and this is a hint toward something greater. It is a standard network protocol that has been developed to allow editors to talk to Repls. jank will use it as well.

We can see this when I jump into a Clojure-code file [opens an editor showing lots of code, i.e. many Clojure functions]. I connect the n-Repl session in my editor via localhost [in-editor window appears on top right]. This Repl session, it has nothing, it doesn't have any of my functions. It does not have anything but I can just send it whatever I want on the fly.

In this particular bit of code [moves cursor in code-file] I have a web server. Let me actually show there's no web server running just right now [opens new terminal, curl localhost:3000, connection refuses]. However, I'm going to send this whole buffer to the Repl and each of these top level expressions was evaluated and we can see the result from each of these evaluations in the top right window.

[note: within the editor, "buffer" means the whole code-text of the code-file, "sending the buffer" means typing some keystroke, e.g. ctrl-s, "top level expression" means the code-text of one single Clojure function]

At the bottom of the file here, I have this start and stop functions. I can just evaluate this start function by sending that to the Repl in this context that I am.

[note: at the beginning, the Repl was empty. But by sending the "buffer" to the Repl, it now has "context", meaning it contains the functionality of a web-server and thus can execute the start function without error. "sending" a single function needs typing a different keystroke, e.g. ctrl-e]

I know our web server has started so now I can curl localhost 3000 and I gat back some JSON-data so that's already a good start in terms of the interactive programming. I just started up my Repl and turned it into whatever I wanted.

If I want to turn my Repl now into something else entirely, a GUI application, I could do that as well. I just got to send that code over.

## [08:30 - 20:00 live demo]

I actually want to demonstrate some real-world programming for what to do in an interactive session. I want to make an HTTP request.

[09:35] Clojure is built around a standard library of immutable persistent data structures, which are transparent.

[10:40 the data structure returned by the HTTP request is stored and referenced in the Repl context] I actually have full access to the data to just work on massaging it. I don't have to keep doing this HTTP request over and over again.

[11:00 writes code into a comment block and sends it to test it, only then wrapping the code into a function] Now this function exists in my process and now I can redefine this function willy-nilly and any reference to it is going to maintain that reference.

[15:06] Every time I send something to the Repl, it's just in time compiled. In the case of JVM Clojure it's being compiled to Java bytecode, in the case of jank it's being compiled using Cling and added to the process.

[16:11] We'll have to accept that I will make errors ... let me see, ah ok here we go. Nice thing about interactive programming is it's very easy to just change your code and reevaluate it and get your results back. This is day-to-day programming.

[18:10] When building programs in a Repl we do need to be mindful of, like, well is this going to work actually when I kill the Repl and I start it up again and I send everything over. That's why we start in a comment block and then promote things to functions and then start hooking things together in a more complete way. So let me just send that whole buffer to the Repl again and verify sanity.

[note: we hit the core difference between Repl and mainstream programming. With Repl programming, the program in memory is not the same program as the one saved on disc storage. We need to distinguish, therefore, between "buffer" and "file". Buffer is code on screen, file is code on disc.]

[19:40] I've used Clojure CLR inside of unity 3D to make games. I'm in my editor just sending code building up a process on the fly.

## [20:00 - 30:00 jank/Clojure deep dive]

[26:10] I want to be able to build game engines or do operating system development with something that at least compiles to native code, so jank has become a Clojure dialect with an LLVM host. Jank has other goals, primarily being a gradual structural type system which we will not cover today.

## [30:00 - 50:20 Q&A]

[33:00] Even in game engines you have varying levels of performance requirements. jank is not trying to compete with real time C++.

[37:00] In Clojure CLR which people are using with the game engine Unity 3D there was some really neat work done by Nasser, called Magic. He added some macros to Clojure so you could wrap them around some code (that you know it's going to be hot) and when it compiles it's optimised in a much better way. Having Lisp-macros, it's very easy to add these things on a code level.

[note: so with its Macro feature, after all some third party apparently has a chance to augment existing parts of jank-code to C++ speed]
