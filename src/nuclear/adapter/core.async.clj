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

(ns nuclear.adapter.core.async
  (:require
   [clojure.core.async :refer [chan <! put! close! go-loop]]
   [nuclear.flux :refer [create]]
   [nuclear.operations :as rx :refer [subscribe!]]
   [nuclear.sink.operations :refer [try-emit-value try-emit-error try-emit-complete]]))

(defn publisher->channel
  ([publisher] (publisher->channel (chan) publisher))
  ([ch publisher]
   (->> (rx/on-error! (fn [_] (close! ch)) publisher)
        (rx/on-complete! #(close! ch))
        (subscribe!
         #(put! ch {:next %})
         #(put! ch {:error %})
         #(put! ch :complete)))
   ch))

(defn channel->publisher [ch]
  (->> (create
        #(go-loop []
           (if-let [x (<! ch)]
             (cond
               (= x :complete) (try-emit-complete %)
               (contains? x :next) (try-emit-value (:next x) %)
               (contains? x :error) (try-emit-error (:error x) %)
               :else (try-emit-value x %))
             (try-emit-complete %))
           (recur)))
       (rx/on-error! (fn [_] (close! ch)))
       (rx/on-complete! #(close! ch))))
