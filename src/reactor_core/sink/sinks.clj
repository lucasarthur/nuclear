(ns reactor-core.sink.sinks
  (:refer-clojure :exclude [empty])
  (:require [reactor-core.util.utils :refer [ms->duration]])
  (:import
   (reactor.core.publisher Sinks)
   (java.time Duration)))

(defn- ->many-spec [spec-type many]
  (cond
    (= spec-type :unicast) (.unicast many)
    (= spec-type :replay) (.replay many)
    :else (.multicast many)))

(defn empty []
  (Sinks/empty))

(defn one []
  (Sinks/one))

(defn many [spec-type]
  (->> (Sinks/many) (->many-spec spec-type)))

(defn on-backpressure-buffer
  ([many-spec] (.onBackpressureBuffer many-spec))
  ([batch-size many-spec] (on-backpressure-buffer batch-size false many-spec))
  ([batch-size auto-cancel? many-spec] (.onBackpressureBuffer many-spec batch-size auto-cancel?)))

(defn on-backpressure-error [unicast-spec]
  (.onBackpressureError unicast-spec))

(defn all-or-nothing [multicast-spec]
  (.directAllOrNothing multicast-spec))

(defn best-effort [multicast-spec]
  (.directBestEffort multicast-spec))

(defn all
  ([replay-spec] (.all replay-spec))
  ([batch-size replay-spec] (.all replay-spec batch-size)))

(defn latest
  ([replay-spec] (.latest replay-spec))
  ([default-value replay-spec] (.latestOrDefault replay-spec default-value)))

(defn limit
  ([max-age replay-spec] (limit Integer/MAX_VALUE replay-spec max-age))
  ([history-size max-age replay-spec] (.limit replay-spec history-size ^Duration (ms->duration max-age))))
