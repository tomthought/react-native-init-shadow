(defproject react-native-init-shadow "0.2.0"
  :license {:name "MIT"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [com.7theta/utilis "1.10.0"]
                 [com.7theta/crusta "0.3.1"]
                 [metosin/malli "0.2.1"]
                 [io.aviso/logging "0.3.2"]
                 [cheshire "5.10.0"]
                 [stencil "0.5.0"]
                 [inflections "0.13.2"]]
  :profiles {:uberjar {:source-paths ["src"]
                       :aot :all
                       :main react-native-init-shadow.core
                       :uberjar-name "react-native-init-shadow.jar"}}
  :repl-options {:init-ns react-native-init-shadow.core})
