(ns dda.c4k-jira.jira
 (:require
  [clojure.spec.alpha :as s]
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.common :as cm]))

(s/def ::fqdn cm/fqdn-string?)
(s/def ::issuer cm/letsencrypt-issuer?)
(s/def ::jira-data-volume-path string?)

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "jira/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (str "letsencrypt-" (name issuer) "-issuer")]
    (->
     (yaml/from-string (yaml/load-resource "jira/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn generate-persistent-volume [config]
  (let [{:keys [jira-data-volume-path]} config]
    (-> 
     (yaml/from-string (yaml/load-resource "jira/persistent-volume.yaml"))
     (assoc-in [:spec :hostPath :path] jira-data-volume-path))))

(defn generate-pod [config]
  (let [{:keys [fqdn]} config]
    (-> (yaml/from-string (yaml/load-resource "jira/pod.yaml"))
        (cm/replace-named-value "FQDN" fqdn))))

(defn generate-pvc []
  (yaml/from-string (yaml/load-resource "jira/pvc.yaml")))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "jira/service.yaml")))
