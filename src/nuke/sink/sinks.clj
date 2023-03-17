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

(ns nuke.sink.sinks
  (:refer-clojure :exclude [empty])
  (:require [nuke.util :refer [ms->duration]])
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
