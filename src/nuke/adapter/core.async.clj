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
