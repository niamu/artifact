(ns artifact.deck-decoder
  (:require [artifact.util :as util]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            #?@(:cljs [[goog.crypt :as crypt]
                       [goog.crypt.base64 :as b64]]))
  #?(:clj (:import [java.util Base64])))

(def version 2)
(def encoded-prefix "ADC")

(def ^:private current-byte-idx (atom 0))

(defn- decode-deck-string
  [deck-code-str]
  (when (string/starts-with? deck-code-str encoded-prefix)
    (->> (string/replace (subs deck-code-str (count encoded-prefix))
                         #"-|_" {"-" "/" "_" "="})
         #?(:clj (.decode (Base64/getDecoder))
            :cljs (b64/decodeStringToByteArray))
         (map #(bit-and % 0xFF)))))

(defn- continue?
  [chunk num-bits]
  (not= 0 (bit-and chunk (bit-shift-left 1 num-bits))))

(defn- read-bits-chunk
  [chunk num-bits curr-shift out-bits]
  (bit-or (bit-shift-left (bit-and chunk (dec (bit-shift-left 1 num-bits)))
                          curr-shift)
          out-bits))

(defn- read-var-encoded-uint32
  [base-value base-bits data idx-atom idx-end]
  (if (or (= 0 base-bits)
          (continue? base-value base-bits))
    (loop [out-value (if (continue? base-value base-bits)
                       (read-bits-chunk base-value base-bits 0 0)
                       0)
           delta-shift base-bits]
      (when (> @idx-atom idx-end)
        (throw (#?(:clj Exception. :cljs js/Error.) "End of block.")))
      (swap! idx-atom inc)
      (cond-> (read-bits-chunk (nth data (dec @idx-atom))
                               7
                               delta-shift
                               out-value)
        (continue? (nth data (dec @idx-atom)) 7) (recur (+ delta-shift 7))))
    (read-bits-chunk base-value base-bits 0 0)))

(defn- read-serialized-card
  [data idx-atom idx-end prev-card-base]
  (when (> @idx-atom idx-end)
    (throw (#?(:clj Exception. :cljs js/Error.) "End of block.")))
  (let [header (nth data @idx-atom)
        has-extended-count? (= (bit-shift-right header 6) 0x03)]
    (swap! idx-atom inc)
    [(if has-extended-count?
       (read-var-encoded-uint32 0 0 data idx-atom idx-end)
       (inc (bit-shift-right header 6)))
     (+ prev-card-base
        (read-var-encoded-uint32 header 5 data idx-atom idx-end))]))

(defn- parse-deck
  [deck-bytes]
  (let [version-and-heroes (nth deck-bytes 0)
        checksum (nth deck-bytes 1)
        string-length (if (> version 1) (nth deck-bytes 2) 0)
        total-card-bytes (- (count deck-bytes) string-length)
        computed-checksum (reduce + (->> (take total-card-bytes deck-bytes)
                                         (drop 3)))
        _ (when-not (= version (bit-shift-right version-and-heroes 4))
            (throw (#?(:clj Exception. :cljs js/Error.)
                    (str "Invalid version: "
                         version " != "
                         (bit-shift-right version-and-heroes 4)))))
        _ (when-not (= checksum (bit-and computed-checksum 0xFF))
            (throw (#?(:clj Exception. :cljs js/Error.) "Invalid checksum.")))
        _ (reset! current-byte-idx 3)
        num-heroes (read-var-encoded-uint32 version-and-heroes
                                            3
                                            deck-bytes
                                            current-byte-idx
                                            total-card-bytes)]
    {:heroes (loop [heroes []
                    curr-hero 0
                    prev-card-base 0]
               (if (< curr-hero num-heroes)
                 (let [[turn id] (read-serialized-card deck-bytes
                                                       current-byte-idx
                                                       total-card-bytes
                                                       prev-card-base)]
                   (recur (conj heroes {:id id :turn turn})
                          (inc curr-hero)
                          id))
                 heroes))
     :cards (loop [cards []
                   prev-card-base 0]
              (if (< @current-byte-idx total-card-bytes)
                (let [[count id] (read-serialized-card deck-bytes
                                                       current-byte-idx
                                                       (count deck-bytes)
                                                       prev-card-base)]
                  (recur (conj cards {:id id :count count})
                         id))
                cards))
     :name (if (< @current-byte-idx (count deck-bytes))
             (-> (->> (drop (- (count deck-bytes) string-length)
                            deck-bytes)
                      (take string-length))
                 #?(:clj (-> byte-array (String. "UTF8"))
                    :cljs (-> clj->js crypt/utf8ByteArrayToString))
                 util/strip-tags)
             "")}))

(defn decode
  [deck-code-str]
  (-> deck-code-str
      decode-deck-string
      parse-deck))

(defn -main [deck-code-str] (pprint/pprint (decode deck-code-str)))
