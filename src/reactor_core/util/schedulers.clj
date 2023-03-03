(ns reactor-core.util.schedulers
  (:import (reactor.core.scheduler Schedulers)))

(def bounded-elastic (Schedulers/boundedElastic))
(def parallel (Schedulers/parallel))
(def immediate (Schedulers/immediate))
(def single (Schedulers/single))
