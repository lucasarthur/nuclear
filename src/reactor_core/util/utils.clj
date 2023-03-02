(ns reactor-core.util.utils
  (:import (java.time Duration Instant)))

(defn array? ^Boolean [x] (.isArray (class x)))

(defn inst->delay [x]
  (-> (Instant/ofEpochMilli x)
      (.minusMillis (System/currentTimeMillis))))

(defn duration ^Duration [d]
  (if (instance? Duration d) d (Duration/ofMillis d)))

(defn delay-duration ^Duration [delay]
  (cond
    (inst? delay) (Duration/ofMillis (inst->delay delay))
    :else (duration delay)))
