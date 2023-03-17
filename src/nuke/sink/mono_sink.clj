(ns nuke.sink.mono-sink
  (:require [nuke.sink.protocols :as p])
  (:import [reactor.core.publisher MonoSink]))

(extend-type MonoSink
  p/EmitOperator
  (-try-emit-empty [sink] (.success sink))
  (-try-emit-value [sink value] (.success sink value))
  (-try-emit-error [sink error] (.error sink error)))
