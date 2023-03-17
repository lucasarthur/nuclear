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

(ns nuke.flux
  (:refer-clojure :exclude [merge concat empty range])
  (:require
   [nuke.protocols :as p]
   [nuke.util.sam :as sam]
   [nuke.util :refer [delay->duration ms->duration array? keyword->enum]])
  (:import
   (reactor.core.publisher Mono Flux)
   (reactor.core.publisher FluxSink$OverflowStrategy)
   (org.reactivestreams Publisher Subscriber)
   (java.util.stream Stream)
   (java.time Duration)))

(defn just [values]
  (Flux/just (to-array values)))

(defn empty []
  (Flux/empty))

(defn never []
  (Flux/never))

(defn error [error-or-fn]
  (Flux/error (if (fn? error-or-fn) (error-or-fn) error-or-fn)))

(defn generate
  ([generator] (Flux/generate (sam/->consumer generator)))
  ([state-supplier generator] (generate state-supplier generator #()))
  ([state-supplier generator state-consumer] (Flux/generate
                                              (sam/->callable state-supplier)
                                              (sam/->bi-function generator)
                                              (sam/->consumer state-consumer))))

(defn interval
  ([period] (Flux/interval (ms->duration period)))
  ([delay period] (Flux/interval (delay->duration delay) (ms->duration period))))

(defn merge [sources]
  (Flux/merge ^Iterable sources))

(defn concat [sources]
  (Flux/concat ^Iterable sources))

(defn range [start count]
  (Flux/range start count))

(defn create
  ([emitter] (create emitter :buffer))
  ([emitter back-pressure] (create emitter back-pressure false))
  ([emitter back-pressure push?] (apply (if push? #(Flux/push %1 %2) #(Flux/create %1 %2))
                                        [(sam/->consumer emitter)
                                         (keyword->enum FluxSink$OverflowStrategy back-pressure)])))

(defn defer [f]
  (Flux/defer (sam/->supplier f)))

(defn using
  ([resource-supplier source-supplier resource-cleanup]
   (using resource-supplier source-supplier resource-cleanup true))
  ([resource-supplier source-supplier resource-cleanup eager?]
   (Flux/using
    (sam/->callable resource-supplier)
    (sam/->function source-supplier)
    (sam/->consumer resource-cleanup)
    eager?)))

(defn using-when
  ([resource-supplier resource-closure async-cleanup]
   (using-when resource-supplier resource-closure async-cleanup (fn [resource _] (async-cleanup resource)) async-cleanup))
  ([resource-supplier resource-closure async-complete async-error async-cancel]
   (Flux/usingWhen
    resource-supplier
    (sam/->function resource-closure)
    (sam/->function async-complete)
    (sam/->bi-function async-error)
    (sam/->function async-cancel))))

(defn zip
  ([sources f]
   (cond
     (= 2 (count sources)) (p/-zip-with (first sources) (second sources) f)
     :else (Flux/zip ^Iterable sources (sam/->function f))))
  ([sources f prefetch]
   (cond
     (array? sources) (Flux/zip (sam/->function f) ^int prefetch ^"[Lorg.reactivestreams.Publisher;" (to-array sources))
     :else (Flux/zip ^Iterable sources ^int prefetch (sam/->function f)))))

(defn- from-dispatcher [x]
  (cond
    (array? x) ::array
    (instance? Iterable x) ::iterable
    :else (type x)))

(derive Iterable ::iterable)
(derive Stream ::stream)
(derive Publisher ::publisher)
(derive ::publisher ::mono)
(derive ::publisher ::flux)
(derive Mono ::mono)
(derive Flux ::flux)

(defmulti from ^Flux from-dispatcher)
(defmethod from ::iterable [i] (Flux/fromIterable i))
(defmethod from ::stream [s] (Flux/fromStream ^Stream s))
(defmethod from ::array [a] (Flux/fromArray a))
(defmethod from ::publisher [p] (Flux/from p))
(defmethod from ::mono [m] m)
(defmethod from ::flux [f] f)

(extend-type Flux
  p/FlatMapOperator
  (-flat-map [flux f] (.flatMap flux (sam/->function f)))
  (-concat-map
    ([flux f] (.concatMap flux (sam/->function f)))
    ([flux f prefetch] (.concatMap flux (sam/->function f) prefetch)))
  (-flat-map-iterable [flux f] (.flatMapIterable flux (sam/->function f)))
  (-flat-map-sequential [flux f] (.flatMapSequential flux (sam/->function f)))
  p/MapOperator
  (-map [flux f] (.map flux (sam/->function f)))
  p/FilterOperator
  (-filter [flux f] (.filter flux (sam/->predicate f)))
  (-filter-when [flux f] (.filterWhen flux (sam/->function f)))
  p/TakeOperator
  (-take [flux n] (.take flux ^long n))
  p/SkipOperator
  (-skip [flux n] (.skip flux ^long n))
  p/ZipOperator
  (-zip-with
    ([flux other] (.zipWith flux ^Publisher other))
    ([flux other f] (.zipWith flux ^Publisher other (sam/->bi-function f))))
  p/HideOperator
  (-hide [flux] (.hide flux))
  p/TakeUntilOperator
  (-take-until [flux other] (.takeUntilOther flux ^Publisher other))
  p/TakeWhileOperator
  (-take-while [flux f] (.takeWhile flux (sam/->predicate f)))
  p/ConcatOperator
  (-concat-values [flux values] (.concatWithValues flux (into-array values)))
  (-concat-with [flux other] (.concatWith flux ^Publisher other))
  p/DefaultIfEmptyOperator
  (-default-if-empty [flux default-value] (.defaultIfEmpty flux default-value))
  p/DoOnOperator
  (-on-complete! [flux f] (.doOnComplete flux (sam/->runnable f)))
  (-after-terminate! [flux f] (.doAfterTerminate flux (sam/->runnable f)))
  (-on-cancel! [flux f] (.doOnCancel flux (sam/->runnable f)))
  (-on-signal! [flux f] (.doOnEach flux (sam/->consumer f)))
  (-on-error!
    ([flux f] (.doOnError flux (sam/->consumer f)))
    ([flux of-type? f] (.doOnError flux (sam/->predicate of-type?) (sam/->consumer f))))
  (-on-next! [flux f] (.doOnNext flux (sam/->consumer f)))
  (-on-request! [flux f] (.doOnRequest flux (sam/->long-consumer f)))
  (-on-subscribe! [flux f] (.doOnSubscribe flux (sam/->consumer f)))
  (-on-terminate! [flux f] (.doOnTerminate flux (sam/->runnable f)))
  (-first! [flux f] (.doFirst flux (sam/->runnable f)))
  (-finally! [flux f] (.doFinally flux (sam/->consumer f)))
  p/SubscribeOperator
  (-subscribe
    [flux on-next on-error on-complete on-subscribe]
    (.subscribe flux
                (sam/->consumer on-next)
                (sam/->consumer on-error)
                (sam/->runnable on-complete)
                (sam/->consumer on-subscribe)))
  (-subscribe-on [flux scheduler] (.subscribeOn flux scheduler))
  (-subscribe-with [s p] ((.subscribe ^Publisher p ^Subscriber s) s))
  p/DelayOperator
  (-delay-elements [flux duration] (.delayElements flux (ms->duration duration)))
  (-delay-sequence [flux duration] (.delaySequence flux (ms->duration duration)))
  p/MergeOperator
  (-merge-with [flux other] (.mergeWith flux ^Publisher other))
  p/OnErrorOperator
  (-on-error-complete
    ([flux] (.onErrorComplete flux))
    ([flux of-type?] (.onErrorComplete flux (sam/->predicate of-type?))))
  (-on-error-continue
    ([flux f] (.onErrorContinue flux (sam/->bi-consumer f)))
    ([flux f of-type?] (.onErrorContinue flux (sam/->predicate of-type?) (sam/->bi-consumer f))))
  (-on-error-stop [flux] (.onErrorStop flux))
  (-on-error-resume
    ([flux f] (.onErrorResume flux (sam/->function f)))
    ([flux f of-type?] (.onErrorResume flux (sam/->predicate of-type?) (sam/->function f))))
  (-on-error-return
    ([flux data] (.onErrorReturn flux data))
    ([flux data of-type?] (.onErrorReturn flux (sam/->predicate of-type?) data)))
  p/PublishOperator
  (-publish [flux transformer] (.publish flux (sam/->function transformer)))
  (-publish-on [flux scheduler] (.publishOn flux scheduler))
  p/RepeatOperator
  (-repeat [flux should-repeat? max-times] (.repeat flux max-times (sam/->boolean-supplier should-repeat?)))
  p/SingleOperator
  (-single
    ([flux] (.single flux))
    ([flux default-value] (.single flux default-value)))
  (-single-or-empty [flux] (.singleOrEmpty flux))
  p/RetryOperator
  (-retry [flux max-retries] (.retry flux max-retries))
  p/ShareOperator
  (-share [flux] (.share flux))
  (-share-next [flux] (.shareNext flux))
  p/SwitchOperator
  (-switch-if-empty [flux alternative] (.switchIfEmpty flux alternative))
  p/CacheOperator
  (-cache [flux ttl max-items] (.cache flux max-items ^Duration (ms->duration ttl)))
  p/ThenOperator
  (-then
    ([flux] (.then flux))
    ([flux other] (.then flux ^Publisher other)))
  (-then-empty [flux other] (.thenEmpty flux ^Publisher other))
  (-then-many [flux other] (.thenMany flux ^Publisher other))
  p/TransformOperator
  (-transform [flux transformer] (.transform flux (sam/->function transformer)))
  (-as [flux transformer] (.as flux (sam/->function transformer)))
  p/SkipLastOperator
  (-skip-last [flux n] (.skipLast flux n))
  p/TakeLastOperator
  (-take-last [flux n] (.takeLast flux n))
  p/IgnoreElementsOperator
  (-ignore-elements [flux] (.ignoreElements flux))
  p/BufferOperator
  (-buffer [flux max-size skip] (.buffer flux max-size skip))
  (-buffer-timeout [flux max-size duration] (.bufferTimeout flux max-size (ms->duration duration)))
  (-buffer-until [flux f] (.bufferUntil flux (sam/->predicate f)))
  (-buffer-while [flux f] (.bufferWhile flux (sam/->predicate f)))
  p/CountOperator
  (-count [flux] (.count flux))
  p/HasElementOperator
  (-has-elements? [flux] (.hasElements flux))
  (-has-element? [flux data] (.hasElement flux data))
  p/CollectOperator
  (-collect-list [flux] (.collectList flux))
  (-collect-map [flux key-fn value-fn] (.collectMap flux (sam/->function key-fn) (sam/->function value-fn)))
  p/MatchOperator
  (-all-match? [flux f] (.all flux (sam/->predicate f)))
  (-any-match? [flux f] (.any flux (sam/->predicate f)))
  p/NextOperator
  (-next [flux] (.next flux))
  p/ParallelOperator
  (-parallel [flux] (.parallel flux))
  p/ReplayOperator
  (-replay [flux] (.replay flux))
  p/DistinctOperator
  (-distinct [flux key-fn] (.distinct flux (sam/->function key-fn)))
  p/BackpressureOperator
  (-on-backpressure-buffer [flux] (.onBackpressureBuffer flux))
  p/ReduceOperator
  (-reduce
    ([flux f] (.reduce flux (sam/->bi-function f)))
    ([flux initial f] (.reduce flux initial (sam/->bi-function f))))
  p/HandleOperator
  (-handle [flux f] (.handle flux (sam/->bi-consumer f))))

(defn- iterable->lazy-seq
  ([iterable]
   (iterable->lazy-seq iterable (.iterator iterable)))
  ([iterable i]
   (lazy-seq (when (.hasNext i) (cons (.next i) (iterable->lazy-seq iterable i))))))

(defn ->lazy-seq [^Flux flux]
  (iterable->lazy-seq (.toIterable flux)))
