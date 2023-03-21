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

(ns nuclear.util.queues
  (:refer-clojure :exclude [empty])
  (:import (reactor.util.concurrent Queues)))

(def empty (-> (Queues/empty) .get))
(def one (-> (Queues/one) .get))
(def small (-> (Queues/small) .get))
(def xs (-> (Queues/xs) .get))
(def unbounded-mp (-> (Queues/unboundedMultiproducer) .get))

(def capacity-unsure (Queues/CAPACITY_UNSURE))
(def xs-buffer-size (Queues/XS_BUFFER_SIZE))
(def small-buffer-size (Queues/SMALL_BUFFER_SIZE))

(defn unbounded
  ([] (-> (Queues/unbounded) .get))
  ([link-size] (-> link-size (Queues/unbounded) .get)))

(defn for-batch-size [batch-size]
  (-> batch-size (Queues/get) .get))

(defn capacity-for [queue]
  (Queues/capacity queue))
