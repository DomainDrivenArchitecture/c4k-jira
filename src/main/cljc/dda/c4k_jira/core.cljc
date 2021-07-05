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

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password]))

(defn k8s-objects [config]
  (into
   []
   (concat [(yaml/to-string (postgres/generate-config))
            (yaml/to-string (postgres/generate-secret config))]
           (when (contains? config :postgres-data-volume-path)
             [(yaml/to-string (postgres/generate-persistent-volume config))])
           [(yaml/to-string (postgres/generate-pvc))
            (yaml/to-string (postgres/generate-deployment))
            (yaml/to-string (postgres/generate-service))]
           (when (contains? config :jira-data-volume-path)
             [(yaml/to-string (jira/generate-persistent-volume config))])
           [(yaml/to-string (jira/generate-pvc))
            (yaml/to-string (jira/generate-pod config))
            (yaml/to-string (jira/generate-service))
            (yaml/to-string (jira/generate-certificate config))
            (yaml/to-string (jira/generate-ingress config))
            (yaml/to-string (jira/generate-service))])))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
