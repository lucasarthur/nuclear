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

(ns nuclear.protocols)

(defprotocol TransformOperator
  (-transform [publisher transformer])
  (-as [publisher transformer])
  (-map [publisher transformer])
  (-flat-map [publisher transformer])
  (-flat-map-iterable [publisher transformer])
  (-flat-map-sequential [publisher transformer])
  (-flat-map-many [publisher transformer])
  (-concat-map [publisher transformer] [publisher transformer prefetch])
  (-reduce [publisher reducer] [publisher initial reducer])
  (-filter [publisher predicate])
  (-filter-when [publisher async-predicate]))

(defprotocol WindowOperator                  ;; mono todo
  (-window [publisher max-size skip])
  (-window-duration
    [publisher duration scheduler]
    [publisher duration every scheduler])
  (-window-timeout
    [publisher max-size max-time scheduler fair-backpressure?])
  (-window-until [publisher predicate cut-before?])
  (-window-until-changed [publisher key-selector key-comparator])
  (-window-while [publisher predicate]))

(defprotocol ConcatOperator
  (-concat-with [publisher other])
  (-concat-values [publisher values])
  (-start-with [publisher values])) ;; mono todo

(defprotocol MergeOperator
  (-merge-with [publisher other]))

(defprotocol HandleOperator
  (-handle [publisher handler]))

(defprotocol NextOperator
  (-next [publisher]))

(defprotocol ParallelOperator
  (-parallel [publisher parallelism prefetch]))

(defprotocol ReplayOperator
  (-replay [publisher max-items ttl scheduler]))

(defprotocol DistinctOperator
  (-distinct [publisher key-selector])
  (-distinct-until-changed [publisher key-selector key-comparator])) ;; todo MONO

(defprotocol BackpressureOperator
  (-on-backpressure-buffer [publisher] [publisher max-size on-overflow])
  (-on-backpressure-drop [publisher] [publisher on-dropped]) ;; todo MONO
  (-on-backpressure-error [publisher]) ;; todo mono
  (-on-backpressure-latest [publisher])) ;; todo mono

(defprotocol SwitchOperator
  (-switch-if-empty [publisher alternative])
  (-default-if-empty [publisher default-value]))

(defprotocol DoOnOperator
  (-on-complete! [publisher runnable])
  (-after-terminate! [publisher runnable])
  (-on-cancel! [publisher runnable])
  (-on-signal! [publisher consumer])
  (-on-error! [publisher consumer] [publisher of-type? consumer])
  (-on-next! [publisher consumer])
  (-on-request! [publisher consumer])
  (-on-subscribe! [publisher consumer])
  (-on-terminate! [publisher runnable])
  (-first! [publisher runnable])
  (-finally! [publisher consumer]))

(defprotocol OnErrorOperator
  (-on-error-complete [publisher] [publisher of-type?])
  (-on-error-continue [publisher consumer] [publisher consumer of-type?])
  (-on-error-stop [publisher])
  (-on-error-resume [publisher handler] [publisher handler of-type?])
  (-on-error-return [publisher data] [publisher data of-type?]))

(defprotocol DelayOperator
  (-delay-elements [publisher duration])
  (-delay-sequence [publisher duration])
  (-delay-subscription [publisher duration-or-publisher])) ;; todo MONO

(defprotocol RepeatOperator
  (-repeat [publisher should-repeat? max-times]))

(defprotocol CountOperator
  (-count [publisher]))

(defprotocol IndexOperator ;; mono todo
  (-index [publisher mapper]))

(defprotocol HasElementOperator
  (-has-elements? [publisher])
  (-has-element? [publisher data]))

(defprotocol CollectOperator
  (-collect [publisher container collector])
  (-collect-list [publisher])
  (-collect-map [publisher key-extractor value-extractor]))

(defprotocol MatchOperator
  (-all-match? [publisher predicate])
  (-any-match? [publisher predicate]))

(defprotocol BufferOperator
  (-buffer [publisher max-size skip])
  (-buffer-timeout [publisher max-size duration])
  (-buffer-until [publisher predicate cut-before?])
  (-buffer-while [publisher predicate]))

(defprotocol SingleOperator
  (-single [publisher] [publisher default-value])
  (-single-or-empty [publisher]))

(defprotocol RetryOperator
  (-retry [publisher max-retries]))

(defprotocol CacheOperator
  (-cache [publisher ttl max-items]))

(defprotocol ThenOperator
  (-then [publisher] [publisher other])
  (-then-return [publisher value])
  (-then-empty [publisher other])
  (-then-many [publisher other]))

(defprotocol ShareOperator
  (-share [publisher])
  (-share-next [publisher]))

(defprotocol IgnoreOperator
  (-ignore-elements [publisher]))

(defprotocol BlockOperator ;; mono todo
  (-block [publisher] [publisher timeout])
  (-block-first [publisher] [publisher timeout])
  (-block-last [publisher] [publisher timeout]))

(defprotocol TimingOperator ;; mono todo
  (-elapsed [publisher scheduler])
  (-timed [publisher scheduler])
  (-timestamp [publisher scheduler]))

(defprotocol SkipOperator
  (-skip [publisher n])
  (-skip-duration [publisher duration])
  (-skip-until [publisher predicate])
  (-skip-while [publisher predicate])
  (-skip-last [publisher n]))

(defprotocol TakeOperator
  (-take [publisher n])
  (-take-last [publisher n])
  (-take-until [publisher predicate])
  (-take-while [publisher predicate]))

(defprotocol ZipOperator
  (-zip-with
    [publisher other]
    [publisher other combinator])
  (-zip-when
    [publisher other-generator]
    [publisher other-generator combinator]))

(defprotocol HideOperator
  (-hide [publisher]))

(defprotocol SubscribeOperator
  (-subscribe [publisher on-next on-error on-complete on-subscribe])
  (-subscribe-on [publisher scheduler])
  (-subscribe-with [subscriber publisher]))

(defprotocol PublishOperator
  (-publish
    [publisher prefetch]
    [publisher transformer prefetch])
  (-publish-on [publisher scheduler prefetch]))
