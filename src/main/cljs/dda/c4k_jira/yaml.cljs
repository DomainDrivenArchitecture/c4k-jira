(ns dda.c4k-jira.yaml
  (:require
   ["js-yaml" :as yaml]
   [shadow.resource :as rc]))

(def postgres-config (rc/inline "postgres/config.yaml"))

(def postgres-secret (rc/inline "postgres/secret.yaml"))

(def postgres-deployment (rc/inline "postgres/deployment.yaml"))

(def postgres-service (rc/inline "postgres/service.yaml"))

(defn load-resource [resource-name]
  (case resource-name
    "postgres/config.yaml" postgres-config
    "postgres/secret.yaml" postgres-secret
    "postgres/deployment.yaml" postgres-deployment
    "postgres/service.yaml" postgres-service))