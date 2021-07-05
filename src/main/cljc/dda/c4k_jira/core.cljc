(ns dda.c4k-jira.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-jira.jira :as jira]
  [dda.c4k-jira.postgres :as postgres]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::jira/fqdn]
                     :opt-un [::jira/issuer ::jira/jira-data-volume-path
                              ::postgres/postgres-data-volume-path]))

(def auth? (s/keys :req-un [::postgres/db-user-name ::postgres/db-user-password]))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n"
     (into
      [(yaml/to-string (postgres/generate-config))
       "---"
       (yaml/to-string (postgres/generate-secret resulting-config))]
      (when-some [{:keys [postgres-data-volume-path]} resulting-config]
        ["---"
         (yaml/to-string (postgres/generate-persistent-volume resulting-config))])
      ["---"
       (yaml/to-string (postgres/generate-pvc))
       "---"
       (yaml/to-string (postgres/generate-deployment))
       "---"
       (yaml/to-string (postgres/generate-service))]
      (when-some [{:keys [jira-data-volume-path]} resulting-config]
        ["---"
         (yaml/to-string (jira/generate-persistent-volume resulting-config))])
      ["---"
       (yaml/to-string (jira/generate-pvc))
       "---"
       (yaml/to-string (jira/generate-pod resulting-config))
       "---"
       (yaml/to-string (jira/generate-service))
       "---"
       (yaml/to-string (jira/generate-certificate resulting-config))
       "---"
       (yaml/to-string (jira/generate-ingress resulting-config))
       "---"
       (yaml/to-string (jira/generate-service))]))))
