(ns artifact.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.net URL]))

(def setids
  ["00"
   "01"])

(defn save-file
  [uri]
  (let [url (URL. uri)
        fullpath (.getPath url)
        path (subs fullpath 0 (inc (string/last-index-of fullpath "/")))]
    (when-not (.exists (io/file (str "resources" path)))
      (.mkdirs (io/file (str "resources" path))))
    (when-not (.exists (io/file (str "resources" fullpath)))
      (with-open [in (io/input-stream uri)
                  out (io/output-stream (str "resources" fullpath))]
        (io/copy in out)))))

(defn process-cards
  [card-list]
  (map (fn [{:keys [large-image mini-image ingame-image] :as card}]
         (doseq [image (concat (vals large-image)
                               (vals mini-image)
                               (vals ingame-image))]
           (save-file image))
         card)
       card-list))

(defn cardset
  [setid]
  (let [{:keys [cdn-root url] :as r}
        (-> (slurp (str "https://playartifact.com/cardset/" setid))
            (json/read-str :key-fn (comp keyword #(string/replace % \_ \-))))]
    (-> (slurp (str cdn-root url))
        (json/read-str :key-fn (comp keyword #(string/replace % \_ \-)))
        (get-in [:card-set :card-list])
        process-cards)))

(defn -main
  [& args]
  (doseq [setid setids]
    (cardset setid)))
