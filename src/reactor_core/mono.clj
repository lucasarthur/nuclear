(ns reactor-core.mono
  (:refer-clojure :exclude [when delay empty])
  (:require
   [reactive-streams.core]
   [reactor-core.protocols :as p]
   [reactor-core.util.sam :as sam]
   [reactor-core.util.utils :as utils])
  (:import
   (reactor.core.publisher Mono)
   (org.reactivestreams Publisher Subscriber)
   (java.time Duration)
   (java.util.concurrent CompletionStage)))

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
  (Mono/delay (utils/delay-duration duration)))

(defn publisher->mono [publisher]
  (Mono/from ^Publisher publisher))

(defn callable->mono [callable]
  (Mono/fromCallable (sam/->callable callable)))

(defn promise->mono [promise]
  (Mono/fromCompletionStage ^CompletionStage promise))

(defn runnable->mono [runnable]
  (Mono/fromRunnable (sam/->runnable runnable)))

(defn supplier->mono [supplier]
  (Mono/fromSupplier (sam/->supplier supplier)))

(defn ignoring-elements [source]
  (Mono/ignoreElements ^Publisher source))

(defn defer [f]
  (Mono/defer (sam/->supplier f)))

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
   (using-when resource-supplier resource-closure async-cleanup (fn [resource _] (async-cleanup resource)) async-cleanup))
  ([resource-supplier resource-closure async-complete async-error async-cancel]
   (Mono/usingWhen
    resource-supplier
    (sam/->function resource-closure)
    (sam/->function async-complete)
    (sam/->bi-function async-error)
    (sam/->function async-cancel))))

(defn when [^Iterable sources]
  (Mono/when sources))

(defn zip [sources f]
  (cond
    (= 2 (count sources)) (p/-zip-with (first sources) (second sources) f)
    :else (Mono/zip ^Iterable sources (sam/->function f))))

(extend-type Mono
  p/FlatMapOperator
  (-flat-map [mono f] (.flatMap mono (sam/->function f)))
  (-flat-map-iterable [mono f] (.flatMapIterable mono (sam/->function f)))
  (-flat-map-many [mono f] (.flatMapMany mono (sam/->function f)))
  p/MapOperator
  (-map [mono f] (.map mono (sam/->function f)))
  p/FilterOperator
  (-filter [mono f] (.filter mono (sam/->predicate f)))
  (-filter-when [mono f] (.filterWhen mono (sam/->function f)))
  p/ZipOperator
  (-zip-with
    ([mono other] (.zipWith mono ^Publisher other))
    ([mono other f] (.zipWith mono ^Publisher other (sam/->bi-function f))))
  (-zip-when
    ([mono f] (.zipWhen mono (sam/->function f)))
    ([mono f c] (.zipWhen mono (sam/->function f) (sam/->bi-function c))))
  p/HideOperator
  (-hide [mono] (.hide mono))
  p/TakeUntilOperator
  (-take-until [mono other] (.takeUntilOther mono ^Publisher other))
  p/ConcatOperator
  (-concat-with [mono other] (.concatWith mono ^Publisher other))
  p/DefaultIfEmptyOperator
  (-default-if-empty [mono default-value] (.defaultIfEmpty mono default-value))
  p/DoOnOperator
  (-after-terminate! [mono f] (.doAfterTerminate mono (sam/->runnable f)))
  (-on-cancel! [mono f] (.doOnCancel mono (sam/->runnable f)))
  (-on-signal! [mono f] (.doOnEach mono (sam/->consumer f)))
  (-on-error!
    ([mono f] (.doOnError mono (sam/->consumer f)))
    ([mono of-type? f] (.doOnError mono (sam/->predicate of-type?) (sam/->consumer f))))
  (-on-next! [mono f] (.doOnNext mono (sam/->consumer f)))
  (-on-request! [mono f] (.doOnRequest mono (sam/->long-consumer f)))
  (-on-subscribe! [mono f] (.doOnSubscribe mono (sam/->consumer f)))
  (-on-terminate! [mono f] (.doOnTerminate mono (sam/->runnable f)))
  (-first! [mono f] (.doFirst mono (sam/->runnable f)))
  (-finally! [mono f] (.doFinally mono (sam/->consumer f)))
  p/SubscribeOperator
  (-subscribe
    [mono on-next on-error on-complete on-subscribe] (.subscribe mono
                                                                 (sam/->consumer on-next)
                                                                 (sam/->consumer on-error)
                                                                 (sam/->runnable on-complete)
                                                                 (sam/->consumer on-subscribe)))
  (-subscribe-on [mono scheduler] (.subscribeOn mono scheduler))
  (-subscribe-with [s p] ((.subscribe ^Publisher p ^Subscriber s) s))
  p/DelayOperator
  (-delay-elements [mono duration] (.delayElement mono (utils/duration duration)))
  p/MergeOperator
  (-merge-with [mono other] (.mergeWith mono ^Publisher other))
  p/OnErrorOperator
  (-on-error-complete
    ([mono] (.onErrorComplete mono))
    ([mono of-type?] (.onErrorComplete mono (sam/->predicate of-type?))))
  (-on-error-continue
    ([mono f] (.onErrorContinue mono (sam/->bi-consumer f)))
    ([mono f of-type?] (.onErrorContinue mono (sam/->predicate of-type?) (sam/->bi-consumer f))))
  (-on-error-stop [mono] (.onErrorStop mono))
  (-on-error-resume
    ([mono f] (.onErrorResume mono (sam/->function f)))
    ([mono f of-type?] (.onErrorResume mono (sam/->predicate of-type?) (sam/->function f))))
  (-on-error-return
    ([mono data] (.onErrorReturn mono data))
    ([mono data of-type?] (.onErrorReturn mono (sam/->predicate of-type?) data)))
  p/PublishOperator
  (-publish [mono transformer] (.publish mono (sam/->function transformer)))
  (-publish-on [mono scheduler] (.publishOn mono scheduler))
  p/IgnoreElementsOperator
  (-ignore-elements [mono] (.ignoreElement mono))
  p/RepeatOperator
  (-repeat [mono should-repeat? max-times] (.repeat mono max-times (sam/->boolean-supplier should-repeat?)))
  p/SingleOperator
  (-single
    ([mono] (.single mono))
    ([mono _] (p/-single mono)))
  p/RetryOperator
  (-retry [mono max-retries] (.retry mono max-retries))
  p/ShareOperator
  (-share [mono] (.share mono))
  p/SwitchOperator
  (-switch-if-empty [mono alternative] (.switchIfEmpty mono alternative))
  p/CacheOperator
  (-cache [mono ttl _] (.cache mono ^Duration (utils/duration ttl)))
  p/ThenOperator
  (-then
    ([mono] (.then mono))
    ([mono other] (.then mono ^Publisher other)))
  (-then-empty [mono other] (.thenEmpty mono ^Publisher other))
  (-then-many [mono other] (.thenMany mono ^Publisher other))
  (-then-return [mono default-value] (.thenReturn mono default-value))
  p/TransformOperator
  (-transform [mono transformer] (.transform mono (sam/->function transformer)))
  (-as [mono transformer] (.as mono (sam/->function transformer)))
  p/HasElementOperator
  (-has-elements? [mono] (.hasElement mono))
  (-has-element? [mono data] (p/-map mono #(= data %)))
  p/HandleOperator
  (-handle [mono f] (.handle mono (sam/->bi-consumer f))))

(defn ->flux [^Mono mono]
  (.flux mono))

(defn ->promise ^CompletionStage [^Mono mono]
  (.toFuture mono))
