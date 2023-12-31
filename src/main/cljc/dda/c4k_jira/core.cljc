(ns dda.c4k-jira.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-jira.jira :as jira]
  [dda.c4k-jira.backup :as backup]))

(def default-storage-class :local-path)

(def default-jira-storage-size-gb 50)

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::fqdn]
                     :opt-un [::issuer ::restic-repository]))

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password
                            ::aws-access-key-id ::aws-secret-access-key
                            ::restic-password]))

(defn-spec k8s-objects any?
  [config (s/merge config? auth?)]
  (into
   []
   (concat [(yaml/to-string (postgres/generate-config {:postgres-size :2gb :db-name "jira"}))
            (yaml/to-string (postgres/generate-secret config))
            (yaml/to-string (postgres/generate-pvc {:pv-storage-size-gb 30
                                                    :pvc-storage-class-name default-storage-class}))
            (yaml/to-string (postgres/generate-deployment :postgres-image "postgres:14"))
            (yaml/to-string (postgres/generate-service))
            (yaml/to-string (jira/generate-pvc {:pvc-storage-class-name default-storage-class
                                                :pv-storage-size-gb default-jira-storage-size-gb}))
            (yaml/to-string (jira/generate-deployment config))
            (yaml/to-string (jira/generate-service))
            (yaml/to-string (jira/generate-certificate config))
            (yaml/to-string (jira/generate-ingress config))]
           (when (contains? config :restic-repository)
             [(yaml/to-string (backup/generate-config config))
              (yaml/to-string (backup/generate-secret config))
              (yaml/to-string (backup/generate-cron))
              (yaml/to-string (backup/generate-backup-restore-deployment))]))))

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
