(ns nuke.sink.operations
  (:require
   [nuke.sink.flux_sink]
   [nuke.sink.mono_sink]
   [nuke.sink.empty]
   [nuke.sink.many]
   [nuke.sink.one]
   [nuke.sink.protocols :as p]))

(defn try-emit-value [value sink]
  (p/-try-emit-value sink value))

(defn try-emit-empty [sink]
  (p/-try-emit-empty sink))

(defn try-emit-error
  ([sink] (try-emit-error (ex-info "error" {:cause :unknown}) sink))
  ([error sink] (p/-try-emit-error sink error)))

(defn try-emit-complete [sink]
  (p/-try-emit-complete sink))

(defn subscriber-count [sink]
  (p/-subscriber-count sink))

(defn ->flux [many]
  (.asFlux many))

(defn ->mono [one-or-empty]
  (.asMono one-or-empty))
