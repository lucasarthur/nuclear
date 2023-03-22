(ns nuclear.util.timed
  (:require
   [nuclear.util :refer [str-invoke]]))

(defn value-of [timed]
  (.get timed))

(defn elapsed
  ([timed]
   (elapsed false timed))
  ([since-subscription? timed]
   (str-invoke timed (str
                      "elapsed"
                      (when since-subscription? "SinceSubscription")))))

(defn timestamp [timed]
  (.timestamp timed))
