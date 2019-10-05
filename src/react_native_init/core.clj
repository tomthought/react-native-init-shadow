(ns react-native-init-shadow.core
  (:require [crusta.core :as crusta]
            [clojure.tools.cli :as cli]
            [clojure.java.io :as io]
            [inflections.core :as inflections]
            [stencil.core :as stencil]
            [clojure.string :as st]
            [jsonista.core :as json]))

(def cli-opts
  [["-package" "--package PACKAGE"]])

(def log-lock (Object.))
(defn log
  [& statements]
  (locking log-lock
    (println (st/join " " statements))))

(def template-directory "resources/template")

(defn template-files
  []
  (->> (io/file template-directory)
       (file-seq)
       (remove #(or (.isDirectory %)
                    (re-find #"\.DS_Store" (str %))))))

(defn -main
  [& args]
  (let [{:keys [arguments options summary errors]} (cli/parse-opts args cli-opts)
        project-name (st/trim (str (first arguments)))
        clj-project-name (inflections/hyphenate project-name)]
    (try
      (when (not (seq project-name))
        (throw (ex-info "Must provide project name as first argument.")))
      (log (format "Setting up project '%s'" project-name))
      (let [react-native-module-name (inflections/camel-case clj-project-name :upper)
            p (crusta/exec
               (format
                "react-native init %s --directory %s"
                project-name
                clj-project-name))
            stdout (future
                     (doseq [line (crusta/stdout-seq p)]
                       (log line)))
            stderr (future
                     (doseq [line (crusta/stderr-seq p)]
                       (log line)))]
        @stdout
        @stderr
        @(crusta/run (format "mv %s %s" react-native-module-name clj-project-name))
        @(crusta/run (format "rm -rf %s/__tests__" clj-project-name))
        @(crusta/run (format "rm %s/App.js" clj-project-name))
        @(crusta/run (format "rm %s/index.js" clj-project-name))
        (log "Installing react-dom...")
        @(crusta/run (format "npm --prefix ./%s install react-dom" clj-project-name))
        (log "Copying cljs template files...")
        (let [stencilize #(stencil/render-string
                           %
                           {:clj-project-name clj-project-name
                            :react-native-module-name react-native-module-name})]
          (doseq [template-file (template-files)]
            (let [contents (stencilize (slurp template-file))
                  filename (stencilize
                            (st/replace
                             (str template-file)
                             (re-pattern template-directory)
                             clj-project-name))
                  segments (st/split filename #"/")
                  directories (st/join "/"
                                       (if (re-find #"\." (last segments))
                                         (drop-last segments)
                                         segments))]
              @(crusta/run (format "mkdir -p %s" directories))
              (spit filename contents))))
        (log (format "Project '%s' setup successfully!" project-name)))
      (catch Exception e
        (log (format "An error occurred setting up the project: %s" (str e)))))))
