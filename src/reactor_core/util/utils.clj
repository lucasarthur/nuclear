(ns reactor-core.util.utils
  (:refer-clojure :exclude [replace])
  (:require [clojure.string :refer [replace upper-case]])
  (:import (java.time Duration Instant)))

(defn array? ^Boolean [x] (.isArray (class x)))

(defn inst->delay [x]
  (-> (Instant/ofEpochMilli x)
      (.minusMillis (System/currentTimeMillis))))

(defn ms->duration ^Duration [d]
  (if (instance? Duration d) d (Duration/ofMillis d)))

(defn delay->duration ^Duration [delay]
  (cond
    (inst? delay) (Duration/ofMillis (inst->delay delay))
    :else (ms->duration delay)))

(defn keyword->str [keyword]
  (-> (str keyword)
      (replace ":" "")
      (upper-case)))

(defn keyword->enum [type keyword]
  (Enum/valueOf type (keyword->str keyword)))
