(ns reactor-core.operations
  (:refer-clojure :exclude [reduce distinct next count repeat merge-with filter map mapcat take take-last take-while])
  (:require
   [reactor-core.flux]
   [reactor-core.mono]
   [reactor-core.protocols :as p]))

(defn filter [predicate publisher]
  (p/-filter publisher predicate))

(defn filter-when [f publisher]
  (p/-filter-when publisher f))

(defn skip [n publisher]
  (p/-skip publisher n))

(defn skip-last [n publisher]
  (p/-skip-last publisher n))

(defn take [n publisher]
  (p/-take publisher n))

(defn take-last [n publisher]
  (p/-take-last publisher n))

(defn take-while [f publisher]
  (p/-take-while publisher f))

(defn delay-elements [duration publisher]
  (p/-delay-elements publisher duration))

(defn delay-sequence [duration publisher]
  (p/-delay-sequence publisher duration))

(defn map [f publisher]
  (p/-map publisher f))

(defn mapcat
  ([f publisher] (p/-concat-map publisher f))
  ([f prefetch publisher] (p/-concat-map publisher f prefetch)))

(defn handle [handler publisher]
  (p/-handle publisher handler))

(defn reduce
  ([reducer publisher] (p/-reduce publisher reducer))
  ([reducer initial publisher] (p/-reduce publisher initial reducer)))

(defn concat-values [values publisher]
  (p/-concat-values publisher values))

(defn concat-with [other publisher]
  (p/-concat-with publisher other))

(defn merge-with [other publisher]
  (p/-merge-with publisher other))

(defn flat-map [f publisher]
  (p/-flat-map publisher f))

(defn flat-map-iterable [f publisher]
  (p/-flat-map-iterable publisher f))

(defn flat-map-sequential [f publisher]
  (p/-flat-map-sequential publisher f))

(defn flat-map-many [f publisher]
  (p/-flat-map-many publisher f))

(defn hide [publisher]
  (p/-hide publisher))

(defn take-until [other publisher]
  (p/-take-until publisher other))

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

(defn parallel [publisher]
  (p/-parallel publisher))

(defn replay [publisher]
  (p/-replay publisher))

(defn retry
  ([publisher] (retry Long/MAX_VALUE publisher))
  ([max-retries publisher] (p/-retry publisher max-retries)))

(defn share [publisher]
  (p/-share publisher))

(defn share-next [publisher]
  (p/-share-next publisher))

(defn on-backpressure-buffer [publisher]
  (p/-on-backpressure-buffer publisher))

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

(defn buffer-until [predicate publisher]
  (p/-buffer-until publisher predicate))

(defn buffer-while [predicate publisher]
  (p/-buffer-while publisher predicate))

(defn count [publisher]
  (p/-count publisher))

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
  ([f publisher] (p/-on-error-continue publisher f))
  ([of-type? f publisher] (p/-on-error-continue publisher f of-type?)))

(defn on-error-stop [publisher]
  (p/-on-error-stop publisher))

(defn on-error-resume
  ([f publisher] (p/-on-error-resume publisher f))
  ([of-type? f publisher] (p/-on-error-resume publisher f of-type?)))

(defn on-error-return
  ([data publisher] (p/-on-error-return publisher data))
  ([of-type? data publisher] (p/-on-error-return publisher data of-type?)))

(defn zip-with
  ([other publisher] (p/-zip-with publisher other))
  ([f other publisher] (p/-zip-with publisher other f)))

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
  ([publisher] (publish! identity publisher))
  ([transformer publisher] (p/-publish publisher transformer)))

(defn publish-on [scheduler publisher]
  (p/-publish-on publisher scheduler))

(defn log [publisher]
  (.log publisher))
