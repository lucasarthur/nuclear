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

(ns nuclear.sink.synchronous-sink
  (:require
   [nuclear.sink.protocols :as p])
  (:import
   (reactor.core.publisher SynchronousSink)))

(extend-type SynchronousSink
  p/EmitOperator
  (-try-emit-value [sink value] (.next sink value))
  (-try-emit-complete [sink] (.complete sink))
  (-try-emit-error [sink error] (.error sink error)))
