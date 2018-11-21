(ns artifact.deck-encoder
  (:require [artifact.util :as util]
            [clojure.edn :as edn]
            [clojure.string :as string]
            #?@(:cljs [[goog.crypt :as crypt]
                       [goog.crypt.base64 :as b64]]))
  #?(:clj (:import [java.util Base64])))

(def version 2)
(def encoded-prefix "ADC")
(def header-size 3)

(def ^:private byte-buffer (atom []))

(defn- extract-nbits-with-carry
  [value num-bits]
  (let [limit-bit (bit-shift-left 1 num-bits)
        result (bit-and value (dec limit-bit))]
    (if (>= value limit-bit)
      (bit-or result limit-bit)
      result)))

(defn- encode-bytes->string
  [bs]
  (if (empty? bs)
    (throw (#?(:clj Exception. :cljs js/Error.) "Empty byte buffer."))
    (as-> #?(:clj (.encodeToString (Base64/getEncoder) (byte-array bs))
             :cljs (b64/encodeByteArray bs)) x
      (string/replace x #"/|=" {"/" "-" "=" "_"})
      (str encoded-prefix x))))

(defn- add-to-byte-buffer!
  [b]
  (if (> b 255)
    (throw (#?(:clj Exception. :cljs js/Error.) "Byte out of range."))
    (swap! byte-buffer conj b)))

(defn- add-remaning-number-to-buffer!
  [value already-writtin-bits]
  (loop [v (bit-shift-right value already-writtin-bits)]
    (when (> v 0)
      (add-to-byte-buffer! (extract-nbits-with-carry v 7))
      (recur (bit-shift-right v 7)))))

(defn- add-card-to-buffer!
  [cnt id]
  (let [first-max-byte-cnt 0x03
        extended-cnt (>= (dec cnt) first-max-byte-cnt)
        first-byte-cnt (if extended-cnt first-max-byte-cnt (dec cnt))
        first-byte (bit-or (bit-shift-left first-byte-cnt 6)
                           (extract-nbits-with-carry id 5))]
    (add-to-byte-buffer! first-byte)
    (add-remaning-number-to-buffer! id 5)
    (when extended-cnt
      (add-remaning-number-to-buffer! cnt 0))))

(defn- compute-checksum
  [num-bytes]
  (reduce + 0 (->> (take num-bytes @byte-buffer)
                   (drop header-size))))

(defn- encode-bytes
  [deck-contents]
  (reset! byte-buffer [])
  (let [count-heroes (count (:heroes deck-contents))
        version (bit-or (bit-shift-left version 4)
                        (extract-nbits-with-carry count-heroes 3))
        n (util/strip-tags (:name deck-contents))]
    (add-to-byte-buffer! version)
    (add-to-byte-buffer! 0) ; Dummy checksum byte

    ;; write name size
    (add-to-byte-buffer! (if (> (count n) 63)
                           (count (string/trim (subs n 0 63)))
                           (count n)))
    (add-remaning-number-to-buffer! count-heroes 3)

    ;; write heroes
    (reduce (fn [prev-card-id hero]
              (add-card-to-buffer! (:turn hero) (- (:id hero) prev-card-id))
              (:id hero))
            0
            (sort-by :id (:heroes deck-contents)))

    ;; all other cards
    (reduce (fn [prev-card-id card]
              (add-card-to-buffer! (:count card) (- (:id card) prev-card-id))
              (:id card))
            0
            (sort-by :id (:cards deck-contents)))

    (let [pre-string-byte-count (count @byte-buffer)]
      ;; name
      (doseq [name-byte #?(:clj (.getBytes n)
                           :cljs (crypt/stringToUtf8ByteArray n))]
        (add-to-byte-buffer! name-byte))

      ;; checksum
      (swap! byte-buffer assoc-in [1]
             (bit-and (compute-checksum pre-string-byte-count) 0x0FF)))))

(defn encode
  [deck-contents]
  ;; spec valid deck contents
  (-> deck-contents
      encode-bytes
      encode-bytes->string))

(defn -main
  [deck-contents]
  (println (encode (if (map? deck-contents)
                     deck-contents
                     (edn/read-string deck-contents)))))
