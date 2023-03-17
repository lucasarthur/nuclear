;; Copyright (c) 2023 Lucas Arthur
;;
;; This file is part of Nuke, an open-source library whose goal is to
;; use Project Reactor with Clojure in an idiomatic and simplified way.
;;
;; Nuke is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Nuke is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Nuke. If not, see <http://www.gnu.org/licenses/>.

(ns nuke.util
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
  (if (inst? delay)
    (Duration/ofMillis (inst->delay delay))
    (ms->duration delay)))

(defn keyword->str [keyword]
  (-> (str keyword) (replace ":" "") (upper-case)))

(defn keyword->enum [type keyword]
  (Enum/valueOf type (keyword->str keyword)))
