(ns artifact.util
  (:require [clojure.string :as string]
            [hickory.core :as hickory]))

(defn strip-tags
  [s]
  (->> (hickory/parse-fragment s)
       (map hickory/as-hiccup)
       (map (fn [h] (if-let [[tag attrs content] (and (vector? h) h)]
                     content h)))
       string/join))
