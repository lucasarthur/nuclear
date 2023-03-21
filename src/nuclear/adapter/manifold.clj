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

(ns nuclear.adapter.manifold
  (:require
   [manifold.stream :as s :refer [on-drained consume-async stream put! close! closed?]]
   [nuclear.flux :refer [create]]
   [nuclear.core :as rx :refer [subscribe!]]
   [nuclear.sink.operations :refer [try-emit-value try-emit-error try-emit-complete]]))

(defn publisher->stream
  ([publisher] (publisher->stream (stream) publisher))
  ([s publisher]
   (->> (rx/on-error! (fn [_] (close! s)) publisher)
        (rx/on-complete! #(close! s))
        (subscribe!
         #(put! s {:next %})
         #(put! s {:error %})
         #(put! s :complete)))
   s))

(defn stream->publisher [s]
  (->> (create
        (fn [sink]
          (if (closed? s)
            (try-emit-complete sink)
            (do
              (on-drained s #(try-emit-complete sink))
              (consume-async
               #(cond
                  (= % :complete) (try-emit-complete sink)
                  (contains? % :next) (try-emit-value (:next %) sink)
                  (contains? % :error) (try-emit-error (:error %) sink)
                  :else (try-emit-value % sink))
               s)))))
       (rx/on-error! (fn [_] (close! s)))
       (rx/on-complete! #(close! s))))

(defn ignore-completion-signal [s]
  (s/filter #(not= % :complete) s))

(defn ignore-error-signal [s]
  (s/filter #(not (contains? % :error)) s))

(defn on-complete! [runner s]
  (s/map #(if (= % :complete) (do (runner) %) %) s))

(defn on-error! [handler s]
  (s/map #(if (contains? % :error) (handler %) %) s))
