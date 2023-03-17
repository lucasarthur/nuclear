(ns nuke.sink.many
  (:require [nuke.sink.protocols :as p])
  (:import (reactor.core.publisher Sinks$Many)))

(extend-type Sinks$Many
  p/EmitOperator
  (-try-emit-value [many value] (.tryEmitNext many value))
  (-try-emit-complete [many] (.tryEmitComplete many))
  (-try-emit-error [many error] (.tryEmitError many error))
  p/CountOperator
  (-subscriber-count [many] (.currentSubscriberCount many)))
