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

(ns nuke.adapter.manifold
  (:require
   [manifold.stream :as s :refer [on-drained consume-async stream put! close! closed?]]
   [nuke.flux :refer [create]]
   [nuke.operations :as rx :refer [subscribe!]]
   [nuke.sink.operations :refer [try-emit-value try-emit-error try-emit-complete]]))

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
                  (not (map? %)) (try-emit-value % sink)
                  (contains? % :next) (try-emit-value (:next %) sink)
                  (contains? % :error) (try-emit-error (:error %) sink)
                  :else :invalid)
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
