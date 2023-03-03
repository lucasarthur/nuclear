(ns reactor-core.adapter.flow
  (:import (reactor.adapter JdkFlowAdapter)))

(defn publisher->flow [publisher]
  (JdkFlowAdapter/publisherToFlowPublisher publisher))

(defn flow->flux [flow]
  (JdkFlowAdapter/flowPublisherToFlux flow))
