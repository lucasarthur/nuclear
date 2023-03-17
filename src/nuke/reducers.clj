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

(ns nuke.reducers
  (:refer-clojure :exclude [reduce])
  (:require 
   [nuke.operations :refer [reduce]]
   [clojure.core.protocols :refer [CollReduce]])
  (:import (reactor.core.publisher Flux)))

(extend-protocol CollReduce
  Flux
  (coll-reduce
    ([this f] (reduce f this))
    ([this f start] (reduce f start this))))
