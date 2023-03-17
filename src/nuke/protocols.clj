(ns nuke.protocols)

(defprotocol MapOperator
  (-map [publisher transformer]))

(defprotocol FlatMapOperator
  (-flat-map [publisher transformer])
  (-concat-map [publisher transformer] [publisher transformer prefetch])
  (-flat-map-iterable [publisher transformer])
  (-flat-map-sequential [publisher transformer])
  (-flat-map-many [publisher transformer]))

(defprotocol HandleOperator
  (-handle [publisher handler]))

(defprotocol ConcatOperator
  (-concat-with [publisher other])
  (-concat-values [publisher values]))

(defprotocol NextOperator
  (-next [publisher]))

(defprotocol ParallelOperator
  (-parallel [publisher]))

(defprotocol ReplayOperator
  (-replay [publisher]))

(defprotocol DistinctOperator
  (-distinct [publisher key-extractor]))

(defprotocol BackpressureOperator
  (-on-backpressure-buffer [publisher]))

(defprotocol ReduceOperator
  (-reduce [publisher reducer] [publisher initial reducer]))

(defprotocol MergeOperator
  (-merge-with [publisher other]))

(defprotocol DefaultIfEmptyOperator
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
  (-on-error-resume [publisher f] [publisher f of-type?])
  (-on-error-return [publisher data] [publisher data of-type?]))

(defprotocol FilterOperator
  (-filter [publisher predicate])
  (-filter-when [publisher async-predicate]))

(defprotocol DelayOperator
  (-delay-elements [publisher duration])
  (-delay-sequence [publisher duration]))

(defprotocol RepeatOperator
  (-repeat [publisher should-repeat? max-times]))

(defprotocol CountOperator
  (-count [publisher]))

(defprotocol HasElementOperator
  (-has-elements? [publisher])
  (-has-element? [publisher data]))

(defprotocol CollectOperator
  (-collect-list [publisher])
  (-collect-map [publisher key-extractor value-extractor]))

(defprotocol MatchOperator
  (-all-match? [publisher predicate])
  (-any-match? [publisher predicate]))

(defprotocol BufferOperator
  (-buffer [publisher max-size skip])
  (-buffer-timeout [publisher max-size duration])
  (-buffer-until [publisher predicate])
  (-buffer-while [publisher predicate]))

(defprotocol SingleOperator
  (-single [publisher] [publisher default-value])
  (-single-or-empty [publisher]))

(defprotocol RetryOperator
  (-retry [publisher max-retries]))

(defprotocol SwitchOperator
  (-switch-if-empty [publisher alternative]))

(defprotocol CacheOperator
  (-cache [publisher ttl max-items]))

(defprotocol ThenOperator
  (-then [publisher] [publisher other])
  (-then-return [publisher value])
  (-then-empty [publisher other])
  (-then-many [publisher other]))

(defprotocol TransformOperator
  (-transform [publisher transformer])
  (-as [publisher transformer]))

(defprotocol ShareOperator
  (-share [publisher])
  (-share-next [publisher]))

(defprotocol IgnoreElementsOperator
  (-ignore-elements [publisher]))

(defprotocol SkipOperator
  (-skip [publisher n]))

(defprotocol SkipLastOperator
  (-skip-last [publisher n]))

(defprotocol TakeOperator
  (-take [publisher n]))

(defprotocol TakeLastOperator
  (-take-last [publisher n]))

(defprotocol ZipOperator
  (-zip-with
    [publisher other]
    [publisher other combinator])
  (-zip-when
    [publisher other-generator]
    [publisher other-generator combinator]))

(defprotocol HideOperator
  (-hide [publisher]))

(defprotocol TakeUntilOperator
  (-take-until [publisher other]))

(defprotocol TakeWhileOperator
  (-take-while [publisher predicate]))

(defprotocol SubscribeOperator
  (-subscribe [publisher on-next on-error on-complete on-subscribe])
  (-subscribe-on [publisher scheduler])
  (-subscribe-with [subscriber publisher]))

(defprotocol PublishOperator
  (-publish [publisher transformer])
  (-publish-on [publisher scheduler]))
