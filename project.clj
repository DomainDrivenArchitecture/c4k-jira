(defproject org.domaindrivenarchitecture/c4k-jira "0.1.0-SNAPSHOT"
  :description "jira c4k-installation package"
  :url "https://domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/tools.reader "1.3.4"]
                 [org.domaindrivenarchitecture/c4k-common-clj "0.1.0-SNAPSHOT"]]
  :target-path "target/%s/"
  :source-paths ["src/main/cljc"
                 "src/main/clj"]
  :resource-paths ["src/main/resources"]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:test {:test-paths ["src/test/cljc"]
                    :resource-paths ["src/test/resources"]
                    :dependencies [[dda/data-test "0.1.1"]]}
             :dev {:plugins [[lein-shell "0.5.0"]]}
             :uberjar {:aot :all
                       :main dda.c4k-jira.uberjar
                       :uberjar-name "c4k-jira-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.206"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha4"
                                       :exclusions [com.sun.mail/javax.mail]]
                                      [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["change" "version" "leiningen.release/bump-version"]]
  :aliases {"native" ["shell"
                      "native-image"
                      "--report-unsupported-elements-at-runtime"
                      "--initialize-at-build-time"
                      "-jar" "target/uberjar/c4k-jira-standalone.jar"
                      "-H:ResourceConfigurationFiles=graalvm-resource-config.json"
                      "-H:Log=registerResource"
                      "-H:Name=target/graalvm/${:name}"]})