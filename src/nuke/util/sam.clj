(ns nuke.util.sam
  (:import
   (java.util.function Consumer BiFunction Predicate Function LongFunction BiPredicate LongConsumer Supplier BooleanSupplier BiConsumer)))

(defn- nil-or-sam? [f type] (or (nil? f) (instance? type f)))

(defn ->predicate ^Predicate [f]
  (if (nil-or-sam? f Predicate) f (reify Predicate (test [_ x] (f x)))))

(defn ->bi-predicate ^BiPredicate [f]
  (if (nil-or-sam? f BiPredicate) f (reify BiPredicate (test [_ x y] (f x y)))))

(defn ->function ^Function [f]
  (if (nil-or-sam? f Function) f (reify Function (apply [_ x] (f x)))))

(defn ->long-function ^LongFunction [f]
  (if (nil-or-sam? f LongFunction) f (reify LongFunction (apply [_ x] (f x)))))

(defn ->bi-function ^BiFunction [f]
  (if (nil-or-sam? f BiFunction) f (reify BiFunction (apply [_ x y] (f x y)))))

(defn ->consumer ^Consumer [f]
  (if (nil-or-sam? f Consumer) f (reify Consumer (accept [_ x] (f x)))))

(defn ->long-consumer ^LongConsumer [f]
  (if (nil-or-sam? f LongConsumer) f (reify LongConsumer (accept [_ x] (f x)))))

(defn ->bi-consumer ^BiConsumer [f]
  (if (nil-or-sam? f BiConsumer) f (reify BiConsumer (accept [_ x y] (f x y)))))

(defn ->runnable ^Runnable [f]
  (if (nil-or-sam? f Runnable) f (reify Runnable (run [_] (f)))))

(defn ->callable ^Callable [f]
  (if (nil-or-sam? f Callable) f (reify Callable (call [_] (f)))))

(defn ->supplier ^Supplier [f]
  (if (nil-or-sam? f Supplier) f (reify Supplier (get [_] (f)))))

(defn ->boolean-supplier ^BooleanSupplier [f]
  (if (nil-or-sam? f BooleanSupplier) f (reify BooleanSupplier (getAsBoolean [_] (f)))))
