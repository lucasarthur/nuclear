(ns reactor-core.util.tuples
  (:refer-clojure :exclude [get])
  (:import (reactor.util.function Tuples)))

(defn ->tuple [values]
  (Tuples/fromArray (to-array values)))

(defn get [n tuple]
  (.get tuple n))

(defn ->seq [tuple]
  (iterator-seq (.iterator tuple)))
