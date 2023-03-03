(ns reactor-core.adapter.rx3
  (:require
   [reactor-core.util.utils :refer [keyword->enum]]
   [reactor-core.util.sam :refer [->consumer]])
  (:import
   (reactor.adapter.rxjava RxJava3Adapter)
   (io.reactivex BackpressureStrategy)))

(defn flowable->flux [flowable]
  (RxJava3Adapter/flowableToFlux flowable))

(defn flux->flowable [flux]
  (RxJava3Adapter/fluxToFlowable flux))

(defn mono->flowable [mono]
  (RxJava3Adapter/monoToFlowable mono))

(defn mono->completable
  ([mono] (RxJava3Adapter/monoToCompletable mono))
  ([discard-handler mono] (RxJava3Adapter/monoToCompletable mono (->consumer discard-handler))))

(defn completable->mono [completable]
  (RxJava3Adapter/completableToMono completable))

(defn mono->single [mono]
  (RxJava3Adapter/monoToSingle mono))

(defn single->mono [single]
  (RxJava3Adapter/singleToMono single))

(defn observable->flux
  ([observable] (observable->flux :buffer observable))
  ([strategy observable] (RxJava3Adapter/observableToFlux
                          observable
                          (keyword->enum BackpressureStrategy strategy))))

(defn flux->observable [flux]
  (RxJava3Adapter/fluxToObservable flux))

(defn maybe->mono [maybe]
  (RxJava3Adapter/maybeToMono maybe))

(defn mono->maybe [mono]
  (RxJava3Adapter/monoToMaybe mono))
