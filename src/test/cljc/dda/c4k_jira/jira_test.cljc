(ns dda.c4k-jira.jira-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-jira.jira :as cut]))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1"
          :kind "Certificate"
          :metadata {:name "jira-cert", :namespace "default"}
          :spec
          {:secretName "jira-secret"
           :commonName "xx"
           :duration "2160h",
           :renewBefore "360h",
           :dnsNames ["xx"]
           :issuerRef
           {:name "prod", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "xx" :issuer :prod}))))

(deftest should-generate-pvc
  (is (= {:apiVersion "v1",
          :kind "PersistentVolumeClaim",
          :metadata {:name "jira-pvc", :labels {:app "jira"}},
          :spec
          {:storageClassName "manual",
           :accessModes ["ReadWriteOnce"],
           :resources {:requests {:storage "12Gi"}}}}
         (cut/generate-pvc {:pv-storage-size-gb 12 :pvc-storage-class-name :manual}))))

(deftest should-generate-ingress
  (is (= {:apiVersion "networking.k8s.io/v1"
          :kind "Ingress"
          :metadata
          {:name "ingress-jira"
           :annotations
           {:cert-manager.io/cluster-issuer
            "staging"
            :ingress.kubernetes.io/proxy-body-size "256m"
            :ingress.kubernetes.io/ssl-redirect "true"
            :ingress.kubernetes.io/rewrite-target "/"
            :ingress.kubernetes.io/proxy-connect-timeout "300"
            :ingress.kubernetes.io/proxy-send-timeout "300"
            :ingress.kubernetes.io/proxy-read-timeout "300"}
           :namespace "default"}
          :spec
          {:tls [{:hosts ["xx"], :secretName "jira-secret"}]
           :rules
           [{:host "xx"
             :http
             {:paths
              [{:path "/"
                :pathType "Prefix"
                :backend
                {:service
                 {:name "jira-service",
                  :port {:number 8080}}}}]}}]}}
         (cut/generate-ingress {:fqdn "xx"}))))

(deftest should-generate-deployment
  (is (= {:containers
           [{:image "domaindrivenarchitecture/c4k-jira"
             :name "jira-app"
             :imagePullPolicy "IfNotPresent"
             :env
             [{:name "DB_USERNAME_FILE"
               :value
               "/var/run/secrets/postgres-secret/postgres-user"}
              {:name "DB_PASSWORD_FILE"
               :value
               "/var/run/secrets/postgres-secret/postgres-password"}
              {:name "FQDN", :value "xx"}]
             :command ["/app/entrypoint.sh"]
             :volumeMounts
             [{:mountPath "/var/jira", :name "jira-data-volume"}
              {:name "postgres-secret-volume"
               :mountPath "/var/run/secrets/postgres-secret"
               :readOnly true}]}]
           :volumes
           [{:name "jira-data-volume"
             :persistentVolumeClaim {:claimName "jira-pvc"}}
            {:name "postgres-secret-volume"
             :secret {:secretName "postgres-secret"}}]}
         (get-in (cut/generate-deployment {:fqdn "xx"}) [:spec :template :spec]))))
