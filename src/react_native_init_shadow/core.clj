(ns react-native-init-shadow.core
  (:require [react-native-init-shadow.util.logger :as logger]
            [crusta.core :as crusta]
            [cheshire.core :as json]
            [inflections.core :as inflections]
            [stencil.core :as stencil]
            [malli.core :as m]
            [malli.error :as me]
            [clojure.tools.cli :as cli]
            [clojure.tools.logging :as log]
            [clojure.string :as st]
            [clojure.java.io :as io])
  (:gen-class))

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
  [["-version" "--version VERSION"]])

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

(defn package-json
  [project-name]
  (json/parse-string (slurp (format "%s/package.json" project-name))))

(defn react-version
  [project-name]
  (get-in (package-json project-name) ["dependencies" "react"]))

(defn set-dependency-version
  [project-name dependency version]
  (assoc-in (package-json project-name) ["dependencies" dependency] version))

(defn write-package-json
  [project-name package-json]
  (->> {:pretty (json/create-pretty-printer {:object-field-value-separator ": "})}
       (json/generate-string package-json)
       (spit (format "%s/package.json" project-name))))

(defmulti init-react-native (fn [{:keys [platform]}] platform))

(defmethod init-react-native :mobile
  [{:keys [clj-project-name react-native-module-name version package-name]}]
  (log/info (format "Setting up project '%s'" clj-project-name))
  (exec (apply format
               (str "npx react-native@latest init %s --directory %s"
                    (when version (str " --version %s")))
               (concat
                [react-native-module-name
                 clj-project-name]
                (when version [version]))))
  (when package-name
    (let [package-name (str package-name "." (st/lower-case react-native-module-name))]
      (log/info (format "Setting package name '%s'." package-name))
      @(crusta/run (format "npx react-native-rename@latest %s -b %s" react-native-module-name package-name)
         :directory clj-project-name)))
  @(crusta/run (format "rm -rf %s/__tests__" clj-project-name))
  @(crusta/run (format "rm %s/App.js" clj-project-name))
  @(crusta/run (format "rm %s/index.js" clj-project-name))
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
  (log/info (format "Project '%s' setup successfully!" clj-project-name))
  (log/info "To run your project: ")
  (log/info (format "$ cd %s" clj-project-name))
  (log/info "$ npm install")
  (log/info "$ npx pod-install")
  (log/info "$ shadow-cljs watch dev")
  (log/info "$ npx react-native run-ios # npx react-native run-android"))

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
                  :platform :mobile
                  :clj-project-name clj-project-name
                  :react-native-module-name react-native-module-name
                  :package-name package-name)))
        (catch Exception e
          (let [message (format "An error occurred setting up the project: %s" (.getLocalizedMessage e))]
            (if-let [data (ex-data e)]
              (log/error message (pr-str data))
              (log/error e message))))))))
