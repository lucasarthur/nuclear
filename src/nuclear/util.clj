;; Copyright (c) 2023 Lucas Arthur
;;
;; This file is part of Nuclear, an open-source library whose goal is to
;; use Project Reactor with Clojure in an idiomatic and simplified way.
;;
;; Nuclear is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Nuclear is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Nuclear. If not, see <http://www.gnu.org/licenses/>.

(ns nuclear.util
  (:refer-clojure :exclude [replace])
  (:require
   [clojure.string :refer [replace upper-case lower-case]])
  (:import
   (java.time Duration Instant)
   (clojure.lang Reflector)))

(defn- class-name->kebab [s]
  (-> s (replace #"(?<!^)[A-Z]" #(str "-" %)) lower-case))

(defn- throwable->kw-type [t]
  (-> t .getClass .getSimpleName class-name->kebab keyword))

(defn array? ^Boolean [x]
  (.isArray (class x)))

(defn inst->delay [x]
  (-> (Instant/ofEpochMilli x)
      (.minusMillis (System/currentTimeMillis))))

(defn ms->duration ^Duration [d]
  (if (instance? Duration d) d (Duration/ofMillis d)))

(defn delay->duration ^Duration [delay]
  (if (inst? delay)
    (Duration/ofMillis (inst->delay delay))
    (ms->duration delay)))

(defn keyword->str [keyword]
  (-> (str keyword) (replace ":" "") (upper-case)))

(defn keyword->enum [type keyword]
  (Enum/valueOf type (keyword->str keyword)))

(defn str-invoke [instance method-str & args]
  (Reflector/invokeInstanceMethod instance method-str (to-array args)))

(defn error-of-type? [type e]
  (if-let [data (ex-data e)]
    (= type (:type data))
    (if (instance? Throwable e)
      (= type (throwable->kw-type e))
      false)))
