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

(ns nuke.sink.operations
  (:require
   [nuke.sink.flux_sink]
   [nuke.sink.mono_sink]
   [nuke.sink.empty]
   [nuke.sink.many]
   [nuke.sink.one]
   [nuke.sink.protocols :as p]))

(defn try-emit-value [value sink]
  (p/-try-emit-value sink value))

(defn try-emit-empty [sink]
  (p/-try-emit-empty sink))

(defn try-emit-error
  ([sink] (try-emit-error (ex-info "error" {:cause :unknown}) sink))
  ([error sink] (p/-try-emit-error sink error)))

(defn try-emit-complete [sink]
  (p/-try-emit-complete sink))

(defn subscriber-count [sink]
  (p/-subscriber-count sink))

(defn ->flux [many]
  (.asFlux many))

(defn ->mono [one-or-empty]
  (.asMono one-or-empty))
