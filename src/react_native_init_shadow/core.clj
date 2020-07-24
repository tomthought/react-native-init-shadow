(ns react-native-init-shadow.core
  (:require [react-native-init-shadow.util.logger :as logger]
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

(def template-directory "template")
(def template-files
  ["index.js"
   {:read "gitignore" :write ".gitignore"}
   "shadow-cljs.edn"
   "src/{{clj-project-name}}/core.cljs"
   "README.md"])

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
  [{:keys [clj-project-name react-native-module-name version package-name]}]
  (log/info (format "Setting up project '%s'" clj-project-name))
  (exec (apply format
               (str "react-native init %s --directory %s"
                    (when version (str " --version %s")))
               (concat
                [react-native-module-name
                 clj-project-name]
                (when version [version]))))
  (when package-name
    (let [package-name (str package-name "." react-native-module-name)]
      (log/info (format "Setting package name '%s'." package-name))
      @(crusta/run (format "npx react-native-rename %s -b %s" (str react-native-module-name "Temp") package-name)
         :directory react-native-module-name)
      @(crusta/run (format "npx react-native-rename %s -b %s" react-native-module-name package-name)
         :directory react-native-module-name)))
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
    (doseq [template-file template-files]
      (let [{:keys [read write]} (if (string? template-file)
                                   {:read template-file
                                    :write template-file}
                                   template-file)
            contents (->> (str template-directory "/" read)
                          (io/resource)
                          (slurp)
                          (stencilize))
            filename (stencilize-underscored (str clj-project-name "/" write))
            segments (filter seq (st/split filename #"/"))
            directories (st/join
                         "/" (if (re-find #"\." (last segments))
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
        names (st/split project-name #"/")
        [package-name project-name] (condp = (count names)
                                      1 [nil (first names)]
                                      2 names
                                      (throw (ex-info "Invalid project name" {:project-name project-name})))
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
                  :react-native-module-name react-native-module-name
                  :package-name package-name)))
        (catch Exception e
          (let [message (format "An error occurred setting up the project: %s" (.getLocalizedMessage e))]
            (if-let [data (ex-data e)]
              (log/error message (pr-str data))
              (log/error e message))))))))
