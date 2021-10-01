(ns dda.c4k-jira.core-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-jira.core :as cut]))

(deftest should-k8s-objects
  (is (= 16
         (count (cut/k8s-objects {:fqdn "jira-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "jira"
                                  :postgres-db-password "jira-db-password"
                                  :issuer :prod
                                  :jira-data-volume-path "/var/jira"
                                  :postgres-data-volume-path "/var/postgres"
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"
                                  :restic-repository "restic-repository"}))))
  (is (= 14
         (count (cut/k8s-objects {:fqdn "jira-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "jira"
                                  :postgres-db-password "jira-db-password"
                                  :issuer :prod
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"
                                  :restic-repository "restic-repository"}))))
  (is (= 10
         (count (cut/k8s-objects {:fqdn "jira-neu.prod.meissa-gmbh.de"
                                  :postgres-db-user "jira"
                                  :postgres-db-password "jira-db-password"
                                  :issuer :prod
                                  :aws-access-key-id "aws-id"
                                  :aws-secret-access-key "aws-secret"
                                  :restic-password "restic-pw"})))))
