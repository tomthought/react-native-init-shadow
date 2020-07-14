(defproject react-native-init-shadow "0.1.0"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [com.7theta/utilis "1.9.0"]
                 [com.7theta/crusta "0.3.1"]
                 [metosin/jsonista "0.2.6"]
                 [stencil "0.5.0"]
                 [inflections "0.13.2"]]
  :aot :all
  :source-paths ["src"]
  :main react-native-init-shadow.core
  :repl-options {:init-ns react-native-init-shadow.core})
