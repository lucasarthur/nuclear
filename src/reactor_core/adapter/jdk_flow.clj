(ns reactor-core.adapter.jdk-flow
  (:import (reactor.adapter JdkFlowAdapter)))

(defn publisher->flow [publisher]
  (JdkFlowAdapter/publisherToFlowPublisher publisher))

(defn flow->flux [flow]
  (JdkFlowAdapter/flowPublisherToFlux flow))
