(ns dda.c4k-jira.jira-test
  (:require
   #?(:clj [clojure.test :refer [deftest is are testing run-tests]]
      :cljs [cljs.test :refer-macros [deftest is are testing run-tests]])
   [dda.c4k-jira.jira :as cut]))

(deftest should-generate-certificate
  (is (= {:apiVersion "cert-manager.io/v1alpha2"
          :kind "Certificate"
          :metadata {:name "jira-cert", :namespace "default"}
          :spec
          {:secretName "jira-secret"
           :commonName "xx"
           :dnsNames ["xx"]
           :issuerRef
           {:name "letsencrypt-prod-issuer", :kind "ClusterIssuer"}}}
         (cut/generate-certificate {:fqdn "xx" :issuer :prod}))))

(deftest should-generate-ingress
  (is (= {:apiVersion "extensions/v1beta1"
          :kind "Ingress"
          :metadata
          {:name "ingress-jira"
           :annotations
           {:cert-manager.io/cluster-issuer
            "letsencrypt-staging-issuer"
            :nginx.ingress.kubernetes.io/proxy-body-size "256m"
            :nginx.ingress.kubernetes.io/ssl-redirect "true"
            :nginx.ingress.kubernetes.io/rewrite-target "/"
            :nginx.ingress.kubernetes.io/proxy-connect-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-send-timeout "300"
            :nginx.ingress.kubernetes.io/proxy-read-timeout "300"}
           :namespace "default"}
          :spec
          {:tls [{:hosts ["xx"], :secretName "jira-secret"}]
           :rules
           [{:host "xx"
             :http
             {:paths
              [{:path "/"
                :backend
                {:serviceName "jira-service", :servicePort 8080}}]}}]}}
         (cut/generate-ingress {:fqdn "xx"}))))

(deftest should-generate-persistent-volume
  (is (= {:kind "PersistentVolume"
          :apiVersion "v1"
          :metadata {:name "jira-pv-volume", :labels {:type "local"}}
          :spec
          {:storageClassName "manual"
           :accessModes ["ReadWriteOnce"]
           :capacity {:storage "30Gi"}
           :hostPath {:path "xx"}}}
         (cut/generate-persistent-volume {:jira-data-volume-path "xx"}))))

(deftest should-generate-pod
  (is (= {:kind "Pod"
          :apiVersion "v1"
          :metadata {:name "jira-app", :labels {:app "jira"}}
          :spec
          {:containers
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
             :secret {:secretName "postgres-secret"}}]}}
         (cut/generate-pod {:fqdn "xx"}))))
