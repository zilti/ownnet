(ns ownnet.core
  (:import [ownnet.java WatchEventReceiver FileWatcher])
  (:require [clojure.java.io :as io]))

(def events (agent []))
(def event-cache (atom []))
(def fileindex (agent {}))

(defn event-filename [a]
  (.. a context toAbsolutePath toFile getAbsolutePath))
(defn event-kind [x]
  (keyword (.. x kind name)))

(def watcher
  (reify WatchEventReceiver
    (receive [self event]
      (send-off events
            (fn [old]
              (conj old
                    {:kind (event-kind event)
                     :file (event-filename event)}))
            ))))

(defn fill-cache []
  (send events
        (fn [state]
          (swap! event-cache
                 #(for [entry state]
                    (conj % entry)))
          [])))

(defn list-directory [path]
  (.listFiles (io/file path)))

(defn recursive-watch [folder]
  (let [folder (if (= (class folder) java.io.File)
                 folder (io/file folder))
        content (->> (.listFiles folder)
                   (filter #(.isDirectory %)))]
    (FileWatcher/watchDir (.getAbsolutePath folder) watcher)
    (for [entry content]
      (recursive-watch entry))))

(defn index-directory [path]
  (send fileindex #(assoc % path []))
  (for [file (.listFiles (io/file path))]
    (send fileindex
          (fn [old]
            (update-in old [path] conj
                       {:name (.getName file)
                        :dir? (.isDirectory file)
                        :modified (.lastModified file)})))))

(defn recursive-index-subdirs [path]
  (for [file (->> (.listFiles (io/file path)) (filter #(.isDirectory %)))]
    (index-directory (.getAbsolutePath file))))
