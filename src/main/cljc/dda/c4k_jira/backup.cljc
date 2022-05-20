(ns dda.c4k-jira.backup
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.base64 :as b64]
  [dda.c4k-common.common :as cm]
  [dda.c4k-common.predicate :as pd]))

(s/def ::aws-access-key-id pd/bash-env-string?)
(s/def ::aws-secret-access-key pd/bash-env-string?)
(s/def ::restic-password pd/bash-env-string?)
(s/def ::restic-repository pd/bash-env-string?)

#?(:cljs
   (defmethod yaml/load-resource :backup [resource-name]
     (case resource-name
       "backup/config.yaml" (rc/inline "backup/config.yaml")
       "backup/cron.yaml" (rc/inline "backup/cron.yaml")
       "backup/secret.yaml" (rc/inline "backup/secret.yaml")
       "backup/backup-restore-deployment.yaml" (rc/inline "backup/backup-restore-deployment.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-config [my-conf]
  (let [{:keys [restic-repository]} my-conf]
    (->
     (yaml/from-string (yaml/load-resource "backup/config.yaml"))
     (cm/replace-key-value :restic-repository restic-repository))))

(defn generate-cron []
   (yaml/from-string (yaml/load-resource "backup/cron.yaml")))

(defn generate-backup-restore-deployment []
  (yaml/from-string (yaml/load-resource "backup/backup-restore-deployment.yaml")))

(defn generate-secret [my-auth]
  (let [{:keys [aws-access-key-id aws-secret-access-key restic-password]} my-auth]
    (->
     (yaml/from-string (yaml/load-resource "backup/secret.yaml"))
     (cm/replace-key-value :aws-access-key-id (b64/encode aws-access-key-id))
     (cm/replace-key-value :aws-secret-access-key (b64/encode aws-secret-access-key))
     (cm/replace-key-value :restic-password (b64/encode restic-password)))))
