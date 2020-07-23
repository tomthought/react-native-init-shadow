(ns react-native-init-shadow.util.template
  (:require [clojure.java.io :as io]))

(def template-directory "resources/template")

(defn template-files
  []
  (->> (io/file template-directory)
       (file-seq)
       (remove #(or (.isDirectory %)
                    (re-find #"\.DS_Store" (str %))))))
