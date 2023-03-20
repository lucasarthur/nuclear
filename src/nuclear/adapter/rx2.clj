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

(ns nuclear.adapter.rx2
  (:require [nuclear.util :refer [keyword->enum]])
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
                          (keyword->enum BackpressureStrategy strategy))))

(defn flux->observable [flux]
  (RxJava2Adapter/fluxToObservable flux))

(defn maybe->mono [maybe]
  (RxJava2Adapter/maybeToMono maybe))

(defn mono->maybe [mono]
  (RxJava2Adapter/monoToMaybe mono))
