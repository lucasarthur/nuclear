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

(ns nuclear.flux
  (:refer-clojure :exclude [merge concat empty range])
  (:require
   [nuclear.protocols :as p]
   [nuclear.util.sam :as sam]
   [nuclear.util :refer [delay->duration ms->duration array? keyword->enum]])
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

(defn error
  ([error-or-fn] (error error-or-fn false))
  ([error-or-fn when-requested?]
   (Flux/error (if (fn? error-or-fn) (error-or-fn) error-or-fn) when-requested?)))

(defn first-with-signal [sources]
  (Flux/firstWithSignal ^Iterable sources))

(defn first-with-value [sources]
  (Flux/firstWithValue ^Iterable sources))

(defn generate
  ([generator] (Flux/generate (sam/->consumer generator)))
  ([state-supplier generator] (generate state-supplier generator (fn [_])))
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
  ([emitter]
   (create emitter :buffer))
  ([emitter backpressure]
   (create emitter backpressure false))
  ([emitter backpressure push?]
   (apply (if push? #(Flux/push %1 %2) #(Flux/create %1 %2))
          [(sam/->consumer emitter)
           (keyword->enum FluxSink$OverflowStrategy backpressure)])))

(defn defer [supplier]
  (Flux/defer (sam/->supplier supplier)))

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
   (using-when
    resource-supplier
    resource-closure
    async-cleanup
    (fn [resource _] (async-cleanup resource))
    async-cleanup))
  ([resource-supplier resource-closure async-complete async-error async-cancel]
   (Flux/usingWhen
    resource-supplier
    (sam/->function resource-closure)
    (sam/->function async-complete)
    (sam/->bi-function async-error)
    (sam/->function async-cancel))))

(defn zip
  ([sources combinator]
   (if (= 2 (count sources))
     (p/-zip-with (first sources) (second sources) combinator)
     (Flux/zip ^Iterable sources (sam/->function combinator))))
  ([sources combinator prefetch]
   (Flux/zip ^Iterable sources prefetch (sam/->function combinator))))

(derive Iterable ::iterable)
(derive Stream ::stream)
(derive Publisher ::publisher)
(derive ::publisher ::mono)
(derive ::publisher ::flux)
(derive Mono ::mono)
(derive Flux ::flux)

(defn- from-dispatcher [x]
  (cond
    (array? x) ::array
    (instance? Iterable x) ::iterable
    :else (type x)))

(defmulti from ^Flux from-dispatcher)

(defmethod from ::iterable [i] (Flux/fromIterable i))
(defmethod from ::stream [s] (Flux/fromStream ^Stream s))
(defmethod from ::array [a] (Flux/fromArray a))
(defmethod from ::publisher [p] (Flux/from p))
(defmethod from ::mono [m] m)
(defmethod from ::flux [f] f)

(extend-type Flux
  p/TransformOperator
  (-transform [flux transformer] (.transform flux (sam/->function transformer)))
  (-as [flux transformer] (.as flux (sam/->function transformer)))
  (-map [flux mapper] (.map flux (sam/->function mapper)))
  (-flat-map [flux mapper] (.flatMap flux (sam/->function mapper)))
  (-concat-map
    ([flux mapper] (.concatMap flux (sam/->function mapper)))
    ([flux mapper prefetch] (.concatMap flux (sam/->function mapper) prefetch)))
  (-flat-map-iterable [flux mapper] (.flatMapIterable flux (sam/->function mapper)))
  (-flat-map-sequential [flux mapper] (.flatMapSequential flux (sam/->function mapper)))
  (-filter [flux pred] (.filter flux (sam/->predicate pred)))
  (-filter-when [flux pred] (.filterWhen flux (sam/->function pred)))
  (-reduce
    ([flux reducer] (.reduce flux (sam/->bi-function reducer)))
    ([flux initial reducer] (.reduce flux initial (sam/->bi-function reducer))))

  p/TakeOperator
  (-take [flux n] (.take flux ^long n))
  (-take-until [flux pred] (.takeUntil flux (sam/->predicate pred)))
  (-take-while [flux pred] (.takeWhile flux (sam/->predicate pred)))
  (-take-last [flux n] (.takeLast flux n))

  p/SkipOperator
  (-skip [flux n] (.skip flux ^long n))
  (-skip-duration [flux duration] (.skip flux (ms->duration duration)))
  (-skip-until [flux pred] (.skipUntil flux (sam/->predicate pred)))
  (-skip-while [flux pred] (.skipWhile flux (sam/->predicate pred)))
  (-skip-last [flux n] (.skipLast flux n))

  p/ZipOperator
  (-zip-with
    ([flux other] (.zipWith flux ^Publisher other))
    ([flux other combinator] (.zipWith flux ^Publisher other (sam/->bi-function combinator))))

  p/HideOperator
  (-hide [flux] (.hide flux))

  p/ConcatOperator
  (-concat-values [flux values] (.concatWithValues flux (into-array values)))
  (-concat-with [flux other] (.concatWith flux ^Publisher other))
  (-start-with [flux values] (.startWith flux ^Iterable values))

  p/SwitchOperator
  (-default-if-empty [flux default-value] (.defaultIfEmpty flux default-value))
  (-switch-if-empty [flux alternative] (.switchIfEmpty flux alternative))

  p/DoOnOperator
  (-on-complete! [flux runnable] (.doOnComplete flux (sam/->runnable runnable)))
  (-after-terminate! [flux runnable] (.doAfterTerminate flux (sam/->runnable runnable)))
  (-on-cancel! [flux runnable] (.doOnCancel flux (sam/->runnable runnable)))
  (-on-signal! [flux consumer] (.doOnEach flux (sam/->consumer consumer)))
  (-on-error!
    ([flux consumer] (.doOnError flux (sam/->consumer consumer)))
    ([flux of-type? consumer] (.doOnError flux (sam/->predicate of-type?) (sam/->consumer consumer))))
  (-on-next! [flux consumer] (.doOnNext flux (sam/->consumer consumer)))
  (-on-request! [flux consumer] (.doOnRequest flux (sam/->long-consumer consumer)))
  (-on-subscribe! [flux consumer] (.doOnSubscribe flux (sam/->consumer consumer)))
  (-on-terminate! [flux runnable] (.doOnTerminate flux (sam/->runnable runnable)))
  (-first! [flux runnable] (.doFirst flux (sam/->runnable runnable)))
  (-finally! [flux consumer] (.doFinally flux (sam/->consumer consumer)))

  p/SubscribeOperator
  (-subscribe [flux on-next on-error on-complete on-subscribe]
    (.subscribe flux
                (sam/->consumer on-next)
                (sam/->consumer on-error)
                (sam/->runnable on-complete)
                (sam/->consumer on-subscribe)))
  (-subscribe-on [flux scheduler] (.subscribeOn flux scheduler))
  (-subscribe-with [s p] ((.subscribe ^Publisher p ^Subscriber s) s))

  p/TimingOperator
  (-elapsed [flux scheduler] (.elapsed flux scheduler))
  (-timed [flux scheduler] (.timed flux scheduler))
  (-timestamp [flux scheduler] (.timestamp flux scheduler))

  p/WindowOperator
  (-window [flux max-size skip] (.window flux max-size skip))
  (-window-duration
    ([flux duration scheduler]
     (.window flux (ms->duration duration) scheduler))
    ([flux duration every scheduler]
     (.window flux (ms->duration duration) (ms->duration every) scheduler)))
  (-window-timeout [flux max-size max-time scheduler fair-backpressure?]
    (.windowTimeout flux max-size (ms->duration max-time) scheduler fair-backpressure?))
  (-window-until [flux predicate cut-before?]
    (.windowUntil flux (sam/->predicate predicate) cut-before?))
  (-window-until-changed [flux key-fn key-comparator]
    (.windowUntilChanged flux (sam/->function key-fn) (sam/->bi-predicate key-comparator)))
  (-window-while [flux predicate] (.windowWhile flux (sam/->predicate predicate)))

  p/DelayOperator
  (-delay-elements [flux duration] (.delayElements flux (ms->duration duration)))
  (-delay-sequence [flux duration] (.delaySequence flux (ms->duration duration)))
  (-delay-subscription [flux duration-or-publisher]
    (->> (if (instance? Publisher duration-or-publisher)
           duration-or-publisher
           (delay->duration duration-or-publisher))
         (.delaySubscription flux)))

  p/MergeOperator
  (-merge-with [flux other] (.mergeWith flux ^Publisher other))

  p/OnErrorOperator
  (-on-error-complete
    ([flux] (.onErrorComplete flux))
    ([flux of-type?] (.onErrorComplete flux (sam/->predicate of-type?))))
  (-on-error-continue
    ([flux consumer] (.onErrorContinue flux (sam/->bi-consumer consumer)))
    ([flux consumer of-type?] (.onErrorContinue flux (sam/->predicate of-type?) (sam/->bi-consumer consumer))))
  (-on-error-stop [flux] (.onErrorStop flux))
  (-on-error-resume
    ([flux handler] (.onErrorResume flux (sam/->function handler)))
    ([flux handler of-type?] (.onErrorResume flux (sam/->predicate of-type?) (sam/->function handler))))
  (-on-error-return
    ([flux data] (.onErrorReturn flux data))
    ([flux data of-type?] (.onErrorReturn flux (sam/->predicate of-type?) data)))

  p/PublishOperator
  (-publish
    ([flux prefetch] (.publish flux prefetch))
    ([flux transformer prefetch] (.publish flux (sam/->function transformer) prefetch)))
  (-publish-on [flux scheduler prefetch] (.publishOn flux scheduler prefetch))

  p/RepeatOperator
  (-repeat [flux should-repeat? max-times]
    (.repeat flux max-times (sam/->boolean-supplier should-repeat?)))

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

  p/CacheOperator
  (-cache [flux ttl max-items] (.cache flux max-items ^Duration (ms->duration ttl)))

  p/ThenOperator
  (-then
    ([flux] (.then flux))
    ([flux other] (.then flux ^Publisher other)))
  (-then-empty [flux other] (.thenEmpty flux ^Publisher other))
  (-then-many [flux other] (.thenMany flux ^Publisher other))

  p/IgnoreOperator
  (-ignore-elements [flux] (.ignoreElements flux))

  p/BlockOperator
  (-block
    ([flux] (p/-block-last flux))
    ([flux timeout] (p/-block-last flux timeout)))
  (-block-first
    ([flux] (.blockFirst flux))
    ([flux timeout] (.blockFirst flux (ms->duration timeout))))
  (-block-last
    ([flux] (.blockLast flux))
    ([flux timeout] (.blockLast flux (ms->duration timeout))))

  p/BufferOperator
  (-buffer [flux max-size skip] (.buffer flux max-size skip))
  (-buffer-timeout [flux max-size duration] (.bufferTimeout flux max-size (ms->duration duration)))
  (-buffer-until [flux pred cut-before?] (.bufferUntil flux (sam/->predicate pred) cut-before?))
  (-buffer-while [flux pred] (.bufferWhile flux (sam/->predicate pred)))

  p/CountOperator
  (-count [flux] (.count flux))

  p/IndexOperator
  (-index [flux index-mapper] (.index flux (sam/->bi-function index-mapper)))

  p/HasElementOperator
  (-has-elements? [flux] (.hasElements flux))
  (-has-element? [flux value] (.hasElement flux value))

  p/CollectOperator
  (-collect [flux container collector]
    (.collect flux (sam/value->supplier container) (sam/->bi-consumer collector)))
  (-collect-list [flux] (.collectList flux))
  (-collect-map [flux key-fn value-fn] (.collectMap flux (sam/->function key-fn) (sam/->function value-fn)))

  p/MatchOperator
  (-all-match? [flux pred] (.all flux (sam/->predicate pred)))
  (-any-match? [flux pred] (.any flux (sam/->predicate pred)))

  p/NextOperator
  (-next [flux] (.next flux))

  p/ParallelOperator
  (-parallel [flux parallelism prefetch]
    (.parallel flux parallelism prefetch))

  p/ReplayOperator
  (-replay [flux max-items ttl scheduler]
    (.replay flux max-items (ms->duration ttl) scheduler))

  p/DistinctOperator
  (-distinct [flux key-fn] (.distinct flux (sam/->function key-fn)))
  (-distinct-until-changed [flux key-fn key-comparator]
    (.distinctUntilChanged flux (sam/->function key-fn) (sam/->bi-predicate key-comparator)))

  p/BackpressureOperator
  (-on-backpressure-buffer
    ([flux]
     (.onBackpressureBuffer flux))
    ([flux max-size on-overflow]
     (.onBackpressureBuffer flux max-size (sam/->consumer on-overflow))))
  (-on-backpressure-drop
    ([flux] (.onBackpressureDrop flux))
    ([flux on-dropped] (.onBackpressureDrop flux (sam/->consumer on-dropped))))
  (-on-backpressure-error [flux] (.onBackpressureError flux))
  (-on-backpressure-latest [flux] (.onBackpressureLatest flux))

  p/HandleOperator
  (-handle [flux handler] (.handle flux (sam/->bi-consumer handler))))

(defn- iterable->lazy-seq
  ([iterable]
   (iterable->lazy-seq iterable (.iterator iterable)))
  ([iterable i]
   (lazy-seq (when (.hasNext i) (cons (.next i) (iterable->lazy-seq iterable i))))))

(defn ->lazy-seq [^Flux flux]
  (iterable->lazy-seq (.toIterable flux)))
