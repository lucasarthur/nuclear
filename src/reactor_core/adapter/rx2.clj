(ns reactor-core.adapter.rx2
  (:require [reactor-core.util.utils :as u])
  (:import
   (reactor.adapter.rxjava RxJava2Adapter)
   (io.reactivex BackpressureStrategy)))

(defn flowable->flux [flowable]
  (RxJava2Adapter/flowableToFlux flowable))

(defn flux->flowable [flux]
  (RxJava2Adapter/fluxToFlowable flux))

(defn mono->flowable [mono]
  (RxJava2Adapter/monoToFlowable mono))

(defn mono->completable [mono]
  (RxJava2Adapter/monoToCompletable mono))

(defn completable->mono [completable]
  (RxJava2Adapter/completableToMono completable))

(defn mono->single [mono]
  (RxJava2Adapter/monoToSingle mono))

(defn single->mono [single]
  (RxJava2Adapter/singleToMono single))

(defn observable->flux
  ([observable] (observable->flux :buffer observable))
  ([strategy observable] (RxJava2Adapter/observableToFlux
                          observable
                          (u/keyword->enum BackpressureStrategy strategy))))

(defn flux->observable [flux]
  (RxJava2Adapter/fluxToObservable flux))

(defn maybe->mono [maybe]
  (RxJava2Adapter/maybeToMono maybe))

(defn mono->maybe [mono]
  (RxJava2Adapter/monoToMaybe mono))
