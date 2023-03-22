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

(ns nuclear.mono
  (:refer-clojure :exclude [when delay empty])
  (:require
   [nuclear.protocols :as p]
   [nuclear.util.sam :as sam]
   [nuclear.util :refer [delay->duration ms->duration]])
  (:import
   (reactor.core.publisher Mono Flux)
   (org.reactivestreams Publisher Subscriber)
   (java.time Duration)
   (java.util.concurrent Future)
   (com.spikhalskiy.futurity Futurity)))

(defn just [data]
  (Mono/just data))

(defn just-or-empty [data]
  (Mono/justOrEmpty data))

(defn empty []
  (Mono/empty))

(defn never []
  (Mono/never))

(defn error [error-or-fn]
  (Mono/error (if (fn? error-or-fn) (error-or-fn) error-or-fn)))

(defn create [emitter]
  (Mono/create (sam/->consumer emitter)))

(defn delay [duration]
  (Mono/delay (delay->duration duration)))

(derive ::publisher ::mono)
(derive ::publisher ::flux)
(derive Mono ::mono)
(derive Flux ::flux)

(defn- from-dispatcher [x]
  (cond
    (future? x) ::promise
    (fn? x) ::supplier
    :else (type x)))

(defmulti from ^Mono from-dispatcher)

(defmethod from ::publisher [p] (Mono/from p))
(defmethod from ::promise [^Future p] (-> p Futurity/shift Mono/fromFuture))
(defmethod from ::supplier [f] (-> f sam/->supplier Mono/fromSupplier))

(defn from-runnable [runnable]
  (Mono/fromRunnable (sam/->runnable runnable)))

(defn ignoring-elements [source]
  (Mono/ignoreElements ^Publisher source))

(defn defer [supplier]
  (Mono/defer (sam/->supplier supplier)))

(defn using
  ([resource-supplier source-supplier resource-cleanup]
   (using resource-supplier source-supplier resource-cleanup true))
  ([resource-supplier source-supplier resource-cleanup eager?]
   (Mono/using
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
   (Mono/usingWhen
    resource-supplier
    (sam/->function resource-closure)
    (sam/->function async-complete)
    (sam/->bi-function async-error)
    (sam/->function async-cancel))))

(defn when [^Iterable sources]
  (Mono/when sources))

(defn zip [sources combinator]
  (if (= 2 (count sources))
    (p/-zip-with (first sources) (second sources) combinator)
    (Mono/zip ^Iterable sources (sam/->function combinator))))

(extend-type Mono
  p/TransformOperator
  (-transform [mono transformer] (.transform mono (sam/->function transformer)))
  (-as [mono transformer] (.as mono (sam/->function transformer)))
  (-map [mono mapper] (.map mono (sam/->function mapper)))
  (-flat-map [mono mapper] (.flatMap mono (sam/->function mapper)))
  (-flat-map-iterable [mono mapper] (.flatMapIterable mono (sam/->function mapper)))
  (-flat-map-many [mono mapper] (.flatMapMany mono (sam/->function mapper)))
  (-filter [mono pred] (.filter mono (sam/->predicate pred)))
  (-filter-when [mono pred] (.filterWhen mono (sam/->function pred)))

  p/ZipOperator
  (-zip-with
    ([mono other] (.zipWith mono ^Publisher other))
    ([mono other combinator] (.zipWith mono ^Publisher other (sam/->bi-function combinator))))
  (-zip-when
    ([mono generator] (.zipWhen mono (sam/->function generator)))
    ([mono generator combinator] (.zipWhen mono (sam/->function generator) (sam/->bi-function combinator))))

  p/HideOperator
  (-hide [mono] (.hide mono))

  p/TakeOperator
  (-take-until [mono other] (.takeUntilOther mono ^Publisher other))

  p/ConcatOperator
  (-concat-with [mono other] (.concatWith mono ^Publisher other))

  p/SwitchOperator
  (-default-if-empty [mono default-value] (.defaultIfEmpty mono default-value))
  (-switch-if-empty [mono alternative] (.switchIfEmpty mono alternative))

  p/DoOnOperator
  (-after-terminate! [mono runnable] (.doAfterTerminate mono (sam/->runnable runnable)))
  (-on-cancel! [mono runnable] (.doOnCancel mono (sam/->runnable runnable)))
  (-on-signal! [mono consumer] (.doOnEach mono (sam/->consumer consumer)))
  (-on-error!
    ([mono consumer] (.doOnError mono (sam/->consumer consumer)))
    ([mono of-type? consumer] (.doOnError mono (sam/->predicate of-type?) (sam/->consumer consumer))))
  (-on-next! [mono consumer] (.doOnNext mono (sam/->consumer consumer)))
  (-on-request! [mono consumer] (.doOnRequest mono (sam/->long-consumer consumer)))
  (-on-subscribe! [mono consumer] (.doOnSubscribe mono (sam/->consumer consumer)))
  (-on-terminate! [mono runnable] (.doOnTerminate mono (sam/->runnable runnable)))
  (-first! [mono runnable] (.doFirst mono (sam/->runnable runnable)))
  (-finally! [mono consumer] (.doFinally mono (sam/->consumer consumer)))

  p/SubscribeOperator
  (-subscribe [mono on-next on-error on-complete on-subscribe]
    (.subscribe mono
                (sam/->consumer on-next)
                (sam/->consumer on-error)
                (sam/->runnable on-complete)
                (sam/->consumer on-subscribe)))
  (-subscribe-on [mono scheduler] (.subscribeOn mono scheduler))
  (-subscribe-with [s p] ((.subscribe ^Publisher p ^Subscriber s) s))

  p/DelayOperator
  (-delay-elements [mono duration] (.delayElement mono (ms->duration duration)))
  (-delay-subscription [mono duration-or-publisher]
    (->> (if (instance? Publisher duration-or-publisher)
           duration-or-publisher
           (delay->duration duration-or-publisher))
         (.delaySubscription mono)))

  p/MergeOperator
  (-merge-with [mono other] (.mergeWith mono ^Publisher other))

  p/OnErrorOperator
  (-on-error-complete
    ([mono] (.onErrorComplete mono))
    ([mono of-type?] (.onErrorComplete mono (sam/->predicate of-type?))))
  (-on-error-continue
    ([mono consumer] (.onErrorContinue mono (sam/->bi-consumer consumer)))
    ([mono consumer of-type?] (.onErrorContinue mono (sam/->predicate of-type?) (sam/->bi-consumer consumer))))
  (-on-error-stop [mono] (.onErrorStop mono))
  (-on-error-resume
    ([mono handler] (.onErrorResume mono (sam/->function handler)))
    ([mono handler of-type?] (.onErrorResume mono (sam/->predicate of-type?) (sam/->function handler))))
  (-on-error-return
    ([mono data] (.onErrorReturn mono data))
    ([mono data of-type?] (.onErrorReturn mono (sam/->predicate of-type?) data)))

  p/PublishOperator
  (-publish [mono transformer] (.publish mono (sam/->function transformer)))
  (-publish-on [mono scheduler] (.publishOn mono scheduler))

  p/PublishOperator
  (-publish [mono transformer _]
    (.publish mono (sam/->function transformer)))
  (-publish-on [mono scheduler _]
    (.publishOn mono scheduler _))

  p/IgnoreOperator
  (-ignore-elements [mono] (.ignoreElement mono))

  p/RepeatOperator
  (-repeat [mono should-repeat? max-times] (.repeat mono max-times (sam/->boolean-supplier should-repeat?)))

  p/SingleOperator
  (-single
    ([mono] (.single mono))
    ([mono default-value]
     (-> (.singleOptional mono)
         (p/-map #(.orElse % default-value)))))
  (-single-or-empty [mono]
    (-> (.singleOptional mono)
        (p/-flat-map (fn [optional]
                       (-> (.map optional (sam/->function just))
                           (.orElse (empty)))))))

  p/RetryOperator
  (-retry [mono max-retries] (.retry mono max-retries))

  p/ShareOperator
  (-share [mono] (.share mono))
  (-share-next [mono] (.share mono))

  p/CacheOperator
  (-cache [mono ttl _] (.cache mono ^Duration (ms->duration ttl)))

  p/ThenOperator
  (-then
    ([mono] (.then mono))
    ([mono other] (.then mono ^Publisher other)))
  (-then-empty [mono other] (.thenEmpty mono ^Publisher other))
  (-then-many [mono other] (.thenMany mono ^Publisher other))
  (-then-return [mono default-value] (.thenReturn mono default-value))

  p/HasElementOperator
  (-has-elements? [mono] (.hasElement mono))
  (-has-element? [mono value] (p/-map mono #(= value %)))

  p/HandleOperator
  (-handle [mono handler] (.handle mono (sam/->bi-consumer handler)))

  p/CountOperator
  (-count [mono] (-> (p/-has-elements? mono) (p/-map #(if % 1 0))))

  p/MatchOperator
  (-all-match? [mono predicate] (p/-map mono predicate))
  (-any-match? [mono predicate] (p/-map mono predicate))

  p/BlockOperator
  (-block
    ([mono] (.block mono))
    ([mono timeout] (.block mono (ms->duration timeout))))
  (-block-first
    ([mono] (p/-block mono))
    ([mono timeout] (p/-block mono timeout)))
  (-block-last
    ([mono] (p/-block mono))
    ([mono timeout] (p/-block mono timeout)))

  p/TimingOperator
  (-elapsed [mono scheduler] (.elapsed mono scheduler))
  (-timed [mono scheduler] (.timed mono scheduler))
  (-timestamp [mono scheduler] (.timestamp mono scheduler)))

(defn ->flux [^Mono mono]
  (.flux mono))

(defn ->promise ^Future [^Mono mono]
  (.toFuture mono))
