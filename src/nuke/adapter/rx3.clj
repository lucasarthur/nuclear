;; Copyright (c) 2023 Lucas Arthur
;;
;; This file is part of Nuke, an open-source library whose goal is to
;; use Project Reactor with Clojure in an idiomatic and simplified way.
;;
;; Nuke is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Nuke is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Nuke. If not, see <http://www.gnu.org/licenses/>.

(ns nuke.adapter.rx3
  (:require
   [nuke.util :refer [keyword->enum]]
   [nuke.util.sam :refer [->consumer]])
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
