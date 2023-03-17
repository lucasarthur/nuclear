(ns nuke.sink.flux-sink
  (:require [nuke.sink.protocols :as p])
  (:import [reactor.core.publisher FluxSink]))

(extend-type FluxSink
  p/EmitOperator
  (-try-emit-value [sink value] (.next sink value))
  (-try-emit-complete [sink] (.complete sink))
  (-try-emit-error [sink error] (.error sink error)))
