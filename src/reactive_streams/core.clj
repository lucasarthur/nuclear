(ns reactive-streams.core)

(defprotocol Subscription
  (request [subscription n])
  (cancel [subscription]))

(defprotocol Subscriber
  (on-subscribe [subscriber ^Subscription subscription])
  (on-next [subscriber item])
  (on-error [subscriber error])
  (on-complete [subscriber]))

(defprotocol Publisher
  (subscribe [publisher ^Subscriber subscriber]))

(extend-protocol Subscription
  org.reactivestreams.Subscription
  (request [this n] (.request this n))
  (cancel [this] (.cancel this)))

(extend-protocol Subscriber
  org.reactivestreams.Subscriber
  (on-subscribe [this s] (.onSubscribe this s))
  (on-next [this v] (.onNext this v))
  (on-error [this e] (.onError this e))
  (on-complete [this] (.onComplete this)))

(extend-protocol Publisher
  org.reactivestreams.Publisher
  (subscribe [this s] (.subscribe this s)))
