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

(ns nuke.util.sam
  (:import
   (java.util.function Consumer BiFunction Predicate Function LongFunction BiPredicate LongConsumer Supplier BooleanSupplier BiConsumer)))

(defn- nil-or-sam? [f type] (or (nil? f) (instance? type f)))

(defn ->predicate ^Predicate [f]
  (if (nil-or-sam? f Predicate) f (reify Predicate (test [_ x] (f x)))))

(defn ->bi-predicate ^BiPredicate [f]
  (if (nil-or-sam? f BiPredicate) f (reify BiPredicate (test [_ x y] (f x y)))))

(defn ->function ^Function [f]
  (if (nil-or-sam? f Function) f (reify Function (apply [_ x] (f x)))))

(defn ->long-function ^LongFunction [f]
  (if (nil-or-sam? f LongFunction) f (reify LongFunction (apply [_ x] (f x)))))

(defn ->bi-function ^BiFunction [f]
  (if (nil-or-sam? f BiFunction) f (reify BiFunction (apply [_ x y] (f x y)))))

(defn ->consumer ^Consumer [f]
  (if (nil-or-sam? f Consumer) f (reify Consumer (accept [_ x] (f x)))))

(defn ->long-consumer ^LongConsumer [f]
  (if (nil-or-sam? f LongConsumer) f (reify LongConsumer (accept [_ x] (f x)))))

(defn ->bi-consumer ^BiConsumer [f]
  (if (nil-or-sam? f BiConsumer) f (reify BiConsumer (accept [_ x y] (f x y)))))

(defn ->runnable ^Runnable [f]
  (if (nil-or-sam? f Runnable) f (reify Runnable (run [_] (f)))))

(defn ->callable ^Callable [f]
  (if (nil-or-sam? f Callable) f (reify Callable (call [_] (f)))))

(defn ->supplier ^Supplier [f]
  (if (nil-or-sam? f Supplier) f (reify Supplier (get [_] (f)))))

(defn ->boolean-supplier ^BooleanSupplier [f]
  (if (nil-or-sam? f BooleanSupplier) f (reify BooleanSupplier (getAsBoolean [_] (f)))))
