(ns react-native-init-shadow.util.logger
  (:import [org.slf4j LoggerFactory]
           [ch.qos.logback.classic Logger Level PatternLayout]
           [ch.qos.logback.core ConsoleAppender]
           [ch.qos.logback.classic.encoder PatternLayoutEncoder]))

(defn init-logger
  [level]
  (let [logger-context (doto (LoggerFactory/getILoggerFactory)
                         (.reset))]
    (doto (.getLogger logger-context (Logger/ROOT_LOGGER_NAME))
      (.addAppender (doto (ConsoleAppender.)
                      (.setContext logger-context)
                      (.setLayout (doto (PatternLayout.)
                                    (.setContext logger-context)
                                    (.setPattern "%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSXXX\", UTC} %level %logger{50} %msg%n")
                                    (.start)))
                      (.start)))
      (.setLevel (condp = level
                   :info Level/INFO
                   :warn Level/WARN
                   :debug Level/DEBUG
                   :error Level/ERROR
                   (throw (ex-info "Unrecognized Level" {:level level})))))))
