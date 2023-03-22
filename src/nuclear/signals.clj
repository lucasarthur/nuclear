(ns nuclear.signals
  (:refer-clojure :exclude [next])
  (:import
   (reactor.core.publisher Signal SignalType)))

(def signal-types
  {:subscribe SignalType/SUBSCRIBE
   :request SignalType/REQUEST
   :cancel SignalType/CANCEL
   :on-subscribe SignalType/ON_SUBSCRIBE
   :on-next SignalType/ON_NEXT
   :on-error SignalType/ON_ERROR
   :on-complete SignalType/ON_COMPLETE
   :after-terminate SignalType/AFTER_TERMINATE
   :current-context SignalType/CURRENT_CONTEXT
   :on-context SignalType/ON_CONTEXT})

(def complete (Signal/complete))

(defn error [e]
  (Signal/error e))

(defn next [value]
  (Signal/next value))

(defn subscribe [subscription]
  (Signal/subscribe subscription))

(defn complete? [signal]
  (.isOnComplete signal))

(defn error? [signal]
  (.isOnError signal))

(defn subscribe? [signal]
  (.isOnSubscribe signal))

(defn next? [signal]
  (.isOnNext signal))

(defn has-value? [signal]
  (.hasValue signal))

(defn has-error? [signal]
  (.hasError signal))

(defn error-from [error-signal]
  (.getThrowable error-signal))

(defn subscription-from [subscription-signal]
  (.getSubscription subscription-signal))

(defn value-from [next-signal]
  (.get next-signal))

(defn type-of [signal]
  (some (fn [[k v]] (when (= (.getType signal) v) k)) signal-types))
