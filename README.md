* force-serialize

An almost exact copy of [[https://github.com/technomancy/serializable-fn][serializable-fn]]. This one however will
redefine the clojure.core functions in order to be effective
EVERYWHERE!

[[./serialize.jpg]]

:  ;; raw Clojure behaviour
:  user> (fn [x] (inc (inc x)))
:  #<user$eval__1750$fn__1751 user$eval__1750$fn__1751@927e4be>
     ^^ ugly java stuff
:  ;; with serializable-fn
:  user> (require '[serializable.fn :as s])
:   
:  user> (def dinc (s/fn [x] (inc (inc x))))
:  (fn [x] (inc (inc x)))
     ^^ More lispy result indeed!
:  user> ((eval (read-string (pr-str dinc))) 0)
:  2


* Usage

Simply require =force-serialize.core=. The namespace /will/
automagically replace the core functions.

* License

Copyright © 2013 Frozenlock

Distributed under the Eclipse Public License, the same as Clojure.
