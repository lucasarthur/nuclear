(ns reactor-core.sink.one
  (:require [reactor-core.sink.protocols :as p])
  (:import (reactor.core.publisher Sinks$One)))

(extend-type Sinks$One
  p/EmitOperator
  (-try-emit-value [one value] (.tryEmitValue one value))
  (-try-emit-empty [one] (.tryEmitEmpty one))
  (-try-emit-error [one error] (.tryEmitError one error))
  p/CountOperator
  (-subscriber-count [one] (.currentSubscriberCount one)))
