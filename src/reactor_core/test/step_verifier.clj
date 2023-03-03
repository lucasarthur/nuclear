(ns reactor-core.test.step-verifier
  (:require
   [reactor-core.util.sam :as sam]
   [reactor-core.util.utils :as u])
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
