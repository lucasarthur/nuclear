(ns nuke.sink.protocols)

(defprotocol EmitOperator
  (-try-emit-value [sink value])
  (-try-emit-empty [sink])
  (-try-emit-error [sink error])
  (-try-emit-complete [sink]))

(defprotocol CountOperator
  (-subscriber-count [sink]))
