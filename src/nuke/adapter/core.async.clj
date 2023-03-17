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

(ns nuke.adapter.core.async
  (:require
   [clojure.core.async :refer [chan <! put! close! go-loop]]
   [nuke.flux :refer [create]]
   [nuke.operations :refer [subscribe!]]
   [nuke.sink.operations :refer [try-emit-value try-emit-error try-emit-complete]]))

(defn publisher->channel
  ([publisher] (publisher->channel (chan) publisher))
  ([ch publisher]
   (subscribe!
    #(put! ch {:next %})
    #(do (put! ch {:error %}) (close! ch))
    #(do (put! ch :complete) (close! ch))
    publisher)
   ch))

(defn channel->publisher [ch]
  (create
   #(go-loop []
      (if-let [x (<! ch)]
        (cond
          (= x :complete) (do (close! ch) (try-emit-complete %))
          (contains? x :next) (try-emit-value (:next x) %)
          (contains? x :error) (do (close! ch) (try-emit-error (:error x) %))
          :else :invalid)
        (try-emit-complete %))
      (recur))))
