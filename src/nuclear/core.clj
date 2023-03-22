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

(ns nuclear.core
  (:refer-clojure :exclude [reduce distinct next count repeat merge-with filter map mapcat take take-last take-while])
  (:require
   [nuclear.flux]
   [nuclear.mono]
   [nuclear.util.schedulers :as schedulers]
   [nuclear.util.queues :as queues]
   [nuclear.util.tuples :refer [->tuple]]
   [nuclear.protocols :as p]))

(defn filter [predicate publisher]
  (p/-filter publisher predicate))

(defn filter-when [predicate publisher]
  (p/-filter-when publisher predicate))

(defn skip [n publisher]
  (p/-skip publisher n))

(defn skip-duration [duration publisher]
  (p/-skip-duration publisher duration))

(defn skip-until [predicate publisher]
  (p/-skip-until publisher predicate))

(defn skip-while [predicate publisher]
  (p/-skip-while publisher predicate))

(defn skip-last [n publisher]
  (p/-skip-last publisher n))

(defn take [n publisher]
  (p/-take publisher n))

(defn take-last [n publisher]
  (p/-take-last publisher n))

(defn take-while [predicate publisher]
  (p/-take-while publisher predicate))

(defn delay-elements [duration publisher]
  (p/-delay-elements publisher duration))

(defn delay-sequence [duration publisher]
  (p/-delay-sequence publisher duration))

(defn delay-subscription [duration-or-publisher publisher]
  (p/-delay-subscription publisher duration-or-publisher))

(defn block
  ([publisher] (p/-block publisher))
  ([timeout publisher] (p/-block publisher timeout)))

(defn block-first
  ([publisher] (p/-block-first publisher))
  ([timeout publisher] (p/-block-first publisher timeout)))

(defn block-last
  ([publisher] (p/-block-last publisher))
  ([timeout publisher] (p/-block-last publisher timeout)))

(defn map [mapper publisher]
  (p/-map publisher mapper))

(defn mapcat
  ([mapper publisher] (p/-concat-map publisher mapper))
  ([mapper prefetch publisher] (p/-concat-map publisher mapper prefetch)))

(defn handle [handler publisher]
  (p/-handle publisher handler))

(defn reduce
  ([reducer publisher] (p/-reduce publisher reducer))
  ([reducer initial publisher] (p/-reduce publisher initial reducer)))

(defn concat-values [values publisher]
  (p/-concat-values publisher values))

(defn concat-with [other publisher]
  (p/-concat-with publisher other))

(defn start-with [values publisher]
  (p/-start-with publisher values))

(defn merge-with [other publisher]
  (p/-merge-with publisher other))

(defn flat-map [mapper publisher]
  (p/-flat-map publisher mapper))

(defn flat-map-iterable [mapper publisher]
  (p/-flat-map-iterable publisher mapper))

(defn flat-map-sequential [mapper publisher]
  (p/-flat-map-sequential publisher mapper))

(defn flat-map-many [mapper publisher]
  (p/-flat-map-many publisher mapper))

(defn hide [publisher]
  (p/-hide publisher))

(defn take-until [predicate publisher]
  (p/-take-until publisher predicate))

(defn default-if-empty [default-value publisher]
  (p/-default-if-empty publisher default-value))

(defn ignore-elements [publisher]
  (p/-ignore-elements publisher))

(defn repeat
  ([publisher] (repeat (constantly true) publisher))
  ([should-repeat? publisher] (repeat should-repeat? Long/MAX_VALUE publisher))
  ([should-repeat? max-times publisher] (p/-repeat publisher should-repeat? max-times)))

(defn next [publisher]
  (p/-next publisher))

(defn parallel
  ([publisher]
   (parallel schedulers/default-pool-size publisher))
  ([parallelism publisher]
   (parallel parallelism queues/small-buffer-size publisher))
  ([parallelism prefetch publisher]
   (p/-parallel publisher parallelism prefetch)))

(defn replay
  ([publisher] (replay Integer/MAX_VALUE publisher))
  ([max-items publisher]
   (replay max-items 0 publisher))
  ([max-items ttl publisher]
   (replay max-items ttl schedulers/parallel publisher))
  ([max-items ttl scheduler publisher]
   (p/-replay publisher max-items ttl scheduler)))

(defn retry
  ([publisher] (retry Long/MAX_VALUE publisher))
  ([max-retries publisher] (p/-retry publisher max-retries)))

(defn share [publisher]
  (p/-share publisher))

(defn share-next [publisher]
  (p/-share-next publisher))

(defn on-backpressure-buffer
  ([publisher]
   (p/-on-backpressure-buffer publisher))
  ([max-size publisher]
   (on-backpressure-buffer max-size nil publisher))
  ([max-size overflow-consumer publisher]
   (p/-on-backpressure-buffer publisher max-size overflow-consumer)))

(defn on-backpressure-drop
  ([publisher]
   (p/-on-backpressure-drop publisher))
  ([on-dropped publisher]
   (p/-on-backpressure-drop publisher on-dropped)))

(defn on-backpressure-error [publisher]
  (p/-on-backpressure-error publisher))

(defn on-backpressure-latest [publisher]
  (p/-on-backpressure-latest publisher))

(defn switch-if-empty [alternative publisher]
  (p/-switch-if-empty publisher alternative))

(defn cache
  ([publisher] (cache Long/MAX_VALUE publisher))
  ([ttl publisher] (cache Integer/MAX_VALUE ttl publisher))
  ([max-items ttl publisher] (p/-cache publisher ttl max-items)))

(defn buffer
  ([publisher] (buffer 0 publisher))
  ([skip publisher] (buffer skip Integer/MAX_VALUE publisher))
  ([skip max-size publisher] (p/-buffer publisher max-size skip)))

(defn buffer-timeout [max-size duration publisher]
  (p/-buffer-timeout publisher max-size duration))

(defn buffer-until
  ([predicate publisher]
   (buffer-until predicate false publisher))
  ([predicate cut-before? publisher]
   (p/-buffer-until publisher predicate cut-before?)))

(defn buffer-while [predicate publisher]
  (p/-buffer-while publisher predicate))

(defn count [publisher]
  (p/-count publisher))

(defn index
  ([publisher] (index #(->tuple [%1 %2]) publisher))
  ([index-mapper publisher] (p/-index publisher index-mapper)))

(defn then
  ([publisher] (p/-then publisher))
  ([other publisher] (p/-then publisher other)))

(defn then-many [other publisher]
  (p/-then-many publisher other))

(defn then-empty [other publisher]
  (p/-then-empty publisher other))

(defn then-return [default-value publisher]
  (p/-then-return publisher default-value))

(defn transform [transformer publisher]
  (p/-transform publisher transformer))

(defn as [transformer publisher]
  (p/-as publisher transformer))

(defn distinct
  ([publisher] (distinct identity publisher))
  ([key-fn publisher] (p/-distinct publisher key-fn)))

(defn distinct-until-changed
  ([publisher] (distinct-until-changed identity publisher))
  ([key-fn publisher] (distinct-until-changed key-fn = publisher))
  ([key-fn key-comparator publisher]
   (p/-distinct-until-changed publisher key-fn key-comparator)))

(defn single
  ([publisher] (p/-single publisher))
  ([default publisher] (p/-single publisher default)))

(defn single-or-empty [publisher]
  (p/-single-or-empty publisher))

(defn has-element? [data publisher]
  (p/-has-element? publisher data))

(defn has-elements? [publisher]
  (p/-has-elements? publisher))

(defn all-match? [predicate publisher]
  (p/-all-match? publisher predicate))

(defn any-match? [predicate publisher]
  (p/-any-match? publisher predicate))

(defn collect [container collector publisher]
  (p/-collect publisher container collector))

(defn collect-list [publisher]
  (p/-collect-list publisher))

(defn collect-map
  ([key-fn publisher] (collect-map key-fn identity publisher))
  ([key-fn value-fn publisher] (p/-collect-map publisher key-fn value-fn)))

(defn on-complete! [runnable publisher]
  (p/-on-complete! publisher runnable))

(defn after-terminate! [runnable publisher]
  (p/-after-terminate! publisher runnable))

(defn on-cancel! [runnable publisher]
  (p/-on-cancel! publisher runnable))

(defn on-signal! [consumer publisher]
  (p/-on-signal! publisher consumer))

(defn on-error!
  ([consumer publisher] (p/-on-error! publisher consumer))
  ([consumer of-type? publisher] (p/-on-error! publisher of-type? consumer)))

(defn on-next! [consumer publisher]
  (p/-on-next! publisher consumer))

(defn on-request! [consumer publisher]
  (p/-on-request! publisher consumer))

(defn on-subscribe! [consumer publisher]
  (p/-on-subscribe! publisher consumer))

(defn on-terminate! [runnable publisher]
  (p/-on-terminate! publisher runnable))

(defn first! [runnable publisher]
  (p/-first! publisher runnable))

(defn finally! [consumer publisher]
  (p/-finally! publisher consumer))

(defn on-error-complete
  ([publisher] (p/-on-error-complete publisher))
  ([of-type? publisher] (p/-on-error-complete publisher of-type?)))

(defn on-error-continue
  ([handler publisher] (p/-on-error-continue publisher handler))
  ([of-type? handler publisher] (p/-on-error-continue publisher handler of-type?)))

(defn on-error-stop [publisher]
  (p/-on-error-stop publisher))

(defn on-error-resume
  ([handler publisher] (p/-on-error-resume publisher handler))
  ([of-type? handler publisher] (p/-on-error-resume publisher handler of-type?)))

(defn on-error-return
  ([data publisher] (p/-on-error-return publisher data))
  ([of-type? data publisher] (p/-on-error-return publisher data of-type?)))

(defn zip-with
  ([other publisher] (p/-zip-with publisher other))
  ([combinator other publisher] (p/-zip-with publisher other combinator)))

(defn zip-when
  ([f publisher] (p/-zip-when publisher f))
  ([combinator f publisher] (p/-zip-when publisher f combinator)))

(defn subscribe!
  ([publisher] (subscribe! nil nil nil publisher))
  ([on-next publisher] (subscribe! on-next nil publisher))
  ([on-next on-error publisher] (subscribe! on-next on-error nil publisher))
  ([on-next on-error on-complete publisher] (subscribe! on-next on-error on-complete nil publisher))
  ([on-next on-error on-complete on-subscribe publisher] (p/-subscribe publisher on-next on-error on-complete on-subscribe)))

(defn subscribe-on [scheduler publisher]
  (p/-subscribe-on publisher scheduler))

(defn subscribe-with [subscriber publisher]
  (p/-subscribe-with subscriber publisher))

(defn publish!
  ([publisher] (publish! queues/small-buffer-size publisher))
  ([prefetch publisher] (p/-publish publisher prefetch)))

(defn transform-and-publish!
  ([transformer publisher]
   (transform-and-publish! transformer queues/small-buffer-size publisher))
  ([transformer prefetch publisher]
   (p/-publish publisher transformer prefetch)))

(defn publish-on
  ([scheduler publisher]
   (publish-on scheduler queues/small-buffer-size publisher))
  ([scheduler prefetch publisher]
   (p/-publish-on publisher scheduler prefetch)))

(defn elapsed
  ([publisher] (elapsed schedulers/parallel publisher))
  ([scheduler publisher] (p/-elapsed publisher scheduler)))

(defn timed
  ([publisher] (timed schedulers/parallel publisher))
  ([clock publisher] (p/-timed publisher clock)))

(defn timestamp
  ([publisher] (timestamp schedulers/parallel publisher))
  ([scheduler publisher] (p/-timestamp publisher scheduler)))

(defn window
  ([max-size publisher] (window max-size 0 publisher))
  ([max-size skip publisher] (p/-window publisher max-size skip)))

(defn window-duration
  ([duration publisher]
   (window-duration duration schedulers/parallel publisher))
  ([duration timer publisher]
   (p/-window-duration publisher duration timer)))

(defn window-every
  ([duration every publisher]
   (window-every duration every schedulers/parallel publisher))
  ([duration every timer publisher]
   (p/-window-duration publisher duration every timer)))

(defn window-timeout
  ([max-size max-time publisher]
   (window-timeout max-size max-time schedulers/parallel publisher))
  ([max-size max-time timer publisher]
   (window-timeout max-size max-time timer false publisher))
  ([max-size max-time timer fair-backpressure? publisher]
   (p/-window-timeout publisher max-size max-time timer fair-backpressure?)))

(defn window-until
  ([predicate publisher]
   (window-until predicate false publisher))
  ([predicate cut-before? publisher]
   (p/-window-until publisher predicate cut-before?)))

(defn window-until-changed
  ([publisher] (window-until-changed identity publisher))
  ([key-fn publisher] (window-until-changed key-fn = publisher))
  ([key-fn key-comparator publisher]
   (p/-window-until-changed publisher key-fn key-comparator)))

(defn window-while [predicate publisher]
  (p/-window-while publisher predicate))

(defn log [publisher]
  (.log publisher))

(defn dispose! [disposable]
  (.dispose disposable))

(defn disposed? [disposable]
  (.isDisposed disposable))
