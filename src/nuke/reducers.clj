(ns nuke.reducers
  (:refer-clojure :exclude [reduce])
  (:require 
   [nuke.operations :refer [reduce]]
   [clojure.core.protocols :refer [CollReduce]])
  (:import (reactor.core.publisher Flux)))

(extend-protocol CollReduce
  Flux
  (coll-reduce
    ([this f] (reduce f this))
    ([this f start] (reduce f start this))))
