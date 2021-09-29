(ns dda.c4k-jira.backup-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-jira.backup :as cut]))


(deftest should-generate-secret
  (is (= {:apiVersion "v1"
          :kind "Secret"
          :metadata {:name "backup-secret"}
          :type "Opaque"
          :data
          {:aws-access-key-id "YXdzLWlk", :aws-secret-access-key "YXdzLXNlY3JldA==", :restic-password "cmVzdGljLXB3"}}
         (cut/generate-secret {:aws-access-key-id "aws-id" :aws-secret-access-key "aws-secret" :restic-password "restic-pw"}))))

(deftest should-generate-config
  (is (= {:apiVersion "v1"
          :kind "ConfigMap"
          :metadata {:name "backup-config"
                     :labels {:app.kubernetes.io/name "backup"
                              :app.kubernetes.io/part-of "jira"}}
          :data
          {:restic-repository "s3:restic-repository"}}
         (cut/generate-config {:restic-repository "s3:restic-repository"}))))

(deftest should-generate-cron
  (is (= {:apiVersion "batch/v1beta1"
          :kind "CronJob"
          :metadata {:name "jira-backup"
                     :labels {:app.kubernetes.part-of "jira"}}
          :spec {:schedule "10 23 * * *"
                 :successfulJobsHistoryLimit 1
                 :failedJobsHistoryLimit 1
                 :jobTemplate
                 {:spec
                  {:template
                   {:spec
                    {:containers
                     [{:name "backup-app"
                       :image "domaindrivenarchitecture/c4k-jira-backup"
                       :imagePullPolicy "IfNotPresent"
                       :command ["/entrypoint.sh"]
                       :env
                       [{:name "AWS_DEFAULT_REGION"
                         :value "eu-central-1"}
                        {:name "AWS_ACCESS_KEY_ID_FILE"
                         :value "/var/run/secrets/backup-secrets/aws-access-key-id"}
                        {:name "AWS_SECRET_ACCESS_KEY_FILE"
                         :value "/var/run/secrets/backup-secrets/aws-secret-access-key"}
                        {:name "RESTIC_REPOSITORY"
                         :valueFrom
                         {:configMapKeyRef
                          {:name "backup-config"
                           :key "restic-repository"}}}
                        {:name "RESTIC_PASSWORD_FILE"
                         :value "/var/run/secrets/backup-secrets/restic-password"}]
                       :volumeMounts
                       [{:name "jira-data-volume"
                         :mountPath "/var/backups"}
                        {:name "backup-secret-volume"
                         :mountPath "/var/run/secrets/backup-secrets"
                         :readOnly true}]}]
                     :volumes
                     [{:name "jira-data-volume"
                       :persistentVolumeClaim
                       {:claimName "jira-pvc"}}
                      {:name "backup-secret-volume"
                       :secret
                       {:secretName "backup-secret"}}]
                     :restartPolicy "OnFailure"}}}}}}
         (cut/generate-cron))))
