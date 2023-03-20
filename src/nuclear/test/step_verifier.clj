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

(ns nuclear.test.step-verifier
  (:require
   [nuclear.util.sam :as sam]
   [nuclear.util :as u])
  (:import
   (reactor.test StepVerifier)
   (org.reactivestreams Publisher)))

(defn create
  ([publisher] (create Long/MAX_VALUE publisher))
  ([n publisher] (StepVerifier/create ^Publisher publisher n)))

(defn consume-next [consumer step]
  (.consumeNextWith step (sam/->consumer consumer)))

(defn assert-next [assertion step]
  (consume-next step assertion))

(defn expect-next-count [n step]
  (.expectNextCount step n))

(defn expect-next-seq [seq step]
  (.expectNextSequence step seq))

(defn expect-next-matches [matches? step]
  (.expectNextMatches step (sam/->predicate matches?)))

(defn consume-subscription [consumer step]
  (.consumeSubscriptionWith step (sam/->consumer consumer)))

(defn consume-error [consumer step]
  (.consumeErrorWith step (sam/->consumer consumer)))

(defn then [task step]
  (.then step (sam/->runnable task)))

(defn then-consume-while
  ([should? step] (.thenConsumeWhile step (sam/->predicate should?)))
  ([should? consumer step] (.thenConsumeWhile step (sam/->predicate should?) (sam/->consumer consumer))))

(defn then-request [n step]
  (.thenRequest step n))

(defn expect-error
  ([step] (.expectError step))
  ([of-type? step] (.expectErrorMatches step (sam/->predicate of-type?))))

(defn expect-error-satisfies [assertion step]
  (.expectErrorSatisfies step (sam/->consumer assertion)))

(defn expect-timeout [duration step]
  (.expectTimeout step (u/ms->duration duration)))

(defn expect-complete [step]
  (.expectComplete step))

(defn verify [step-verifier]
  (.verify step-verifier))

(defn verify-error
  ([step] (.verifyError step))
  ([of-type? step] (.verifyErrorMatches step (sam/->predicate of-type?))))

(defn verify-error-satisfies [assertion step]
  (.verifyErrorSatisfies step (sam/->consumer assertion)))

(defn verify-complete [step]
  (.verifyComplete step))

(defn log [step-verifier]
  (.log step-verifier))
