(ns react-native-init-shadow.core
  (:require [react-native-init-shadow.util.logger :as logger]
            [react-native-init-shadow.util.template :as template]
            [crusta.core :as crusta]
            [inflections.core :as inflections]
            [stencil.core :as stencil]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [clojure.string :as st]
            [clojure.java.io :as io])
  (:gen-class))

(def latest-windows-react-native-version "0.62.2")

(defn validator
  [schema error-message]
  [(m/validator schema) error-message])

(def cli-opts
  [["-version" "--version VERSION"]
   ["-platform" "--platform PLATFORM"
    :default :mobile
    :parse-fn (comp keyword st/lower-case)
    :validate (validator [:enum :mobile :desktop]
                         "Must be one of 'mobile', 'desktop'.")]])

(defn exec
  [command & options]
  (log/info command)
  (let [p (apply crusta/exec command options)
        stdout (future
                 (doseq [line (crusta/stdout-seq p)]
                   (log/info line)))
        stderr (future
                 (doseq [line (crusta/stderr-seq p)]
                   (log/error line)))]
    @stdout
    @stderr))

(defmulti init-react-native (fn [{:keys [platform]}] platform))

(defmethod init-react-native :mobile
  [{:keys [clj-project-name react-native-module-name version]}]
  (log/info (format "Setting up project '%s'" clj-project-name))
  (exec (apply format
               (str "react-native init %s --directory %s"
                    (when version (str " --version %s")))
               (concat
                [react-native-module-name
                 clj-project-name]
                (when version [version]))))
  @(crusta/run (format "mv %s %s" react-native-module-name clj-project-name))
  @(crusta/run (format "rm -rf %s/__tests__" clj-project-name))
  @(crusta/run (format "rm %s/App.js" clj-project-name))
  @(crusta/run (format "rm %s/index.js" clj-project-name))
  (log/info "Installing react and react-dom...")
  @(crusta/run (format "npm --prefix ./%s install react-dom" clj-project-name))
  @(crusta/run (format "npm --prefix ./%s uninstall react" clj-project-name))
  @(crusta/run (format "npm --prefix ./%s install react" clj-project-name))
  (log/info "Copying cljs template files...")
  (let [stencil-props {:clj-project-name clj-project-name
                       :react-native-module-name react-native-module-name}
        stencilize #(stencil/render-string % stencil-props)
        stencilize-underscored #(stencil/render-string
                                 % (update stencil-props :clj-project-name
                                           inflections/underscore))]
    (doseq [template-file (template/template-files)]
      (let [contents (stencilize (slurp template-file))
            filename (stencilize-underscored
                      (st/replace
                       (str template-file)
                       (re-pattern template/template-directory)
                       clj-project-name))
            segments (st/split filename #"/")
            directories (st/join "/"
                                 (if (re-find #"\." (last segments))
                                   (drop-last segments)
                                   segments))]
        @(crusta/run (format "mkdir -p %s" directories))
        (spit filename contents))))
  (log/info (format "Project '%s' setup successfully!" clj-project-name)))

(defmethod init-react-native :desktop
  [{:keys [clj-project-name react-native-module-name version] :as options}]
  (init-react-native
   (cond-> (assoc options
                  :platform :mobile
                  :version latest-windows-react-native-version)
     version (assoc :version version)))
  (log/info "Initializing desktop project...")
  (exec "npx react-native-macos-init" :directory clj-project-name)
  (exec "npx react-native-windows-init --overwrite" :directory clj-project-name)
  (log/info "Initialized desktop project successfully!")
  (log/info "To run your project: ")
  (log/info (format "$ cd %s" clj-project-name))
  (log/info "$ shadow-cljs watch dev")
  (log/info "$ npx react-native run-macos # npx react-native run-windows"))

(defn -main
  [& args]
  (logger/init-logger :info)
  (let [{:keys [arguments options summary errors] :as parsed} (cli/parse-opts args cli-opts)
        project-name (st/trim (str (first arguments)))
        errors (not-empty
                (concat
                 (when (not (seq project-name))
                   ["Must provide project name as first argument."])
                 errors))]
    (if (seq errors)
      (doseq [error errors]
        (log/error error))
      (try
        (let [clj-project-name (inflections/hyphenate project-name)
              react-native-module-name (inflections/camel-case clj-project-name :upper)]
          (when (.exists (io/file clj-project-name))
            (throw (ex-info "Can not overwrite existing directory" {:directory clj-project-name})))
          (when (.exists (io/file react-native-module-name))
            (throw (ex-info "Can not overwrite existing directory" {:directory react-native-module-name})))
          (init-react-native
           (assoc options
                  :clj-project-name clj-project-name
                  :react-native-module-name react-native-module-name)))
        (catch Exception e
          (let [message (format "An error occurred setting up the project: %s" (.getLocalizedMessage e))]
            (if-let [data (ex-data e)]
              (log/error message (pr-str data))
              (log/error e message))))))))
