(ns dda.c4k-jira.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jira.jira :as jira]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::jira/fqdn]
                     :opt-un [::jira/issuer]))

(def auth? (s/keys :req-un []))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config)]
    (cs/join "\n"
             [(yaml/to-string (jira/generate-persistent-volume))
              "---"
              (yaml/to-string (jira/generate-pvc))
              "---"
              (yaml/to-string (jira/generate-certificate resulting-config))
              "---"
              (yaml/to-string (jira/generate-ingress resulting-config))
              "---"
              (yaml/to-string (jira/generate-service))
              "---"
              (yaml/to-string (jira/generate-pod resulting-config))])))
