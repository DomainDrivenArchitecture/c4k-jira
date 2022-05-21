(ns dda.c4k-jira.jira
 (:require
  [clojure.spec.alpha :as s]
  #?(:cljs [shadow.resource :as rc])
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.predicate :as cp]
  [dda.c4k-common.common :as cm]))

(s/def ::fqdn cp/fqdn-string?)
(s/def ::issuer cp/letsencrypt-issuer?)
(s/def ::restic-repository string?)
(s/def ::pvc-storage-class-name cp/pvc-storage-class-name?)
(s/def ::pv-storage-size-gb pos?)

(def strong-config? (s/keys :req-un [::fqdn ::issuer ::pv-storage-size-gb
                                     ::pvc-storage-class-name]
                     :opt-un [::restic-repository]))

#?(:cljs
   (defmethod yaml/load-resource :jira [resource-name]
     (case resource-name
       "jira/certificate.yaml" (rc/inline "jira/certificate.yaml")
       "jira/deployment.yaml" (rc/inline "jira/deployment.yaml")
       "jira/ingress.yaml" (rc/inline "jira/ingress.yaml")
       "jira/pvc.yaml" (rc/inline "jira/pvc.yaml")
       "jira/service.yaml" (rc/inline "jira/service.yaml")
       (throw (js/Error. "Undefined Resource!")))))

(defn generate-certificate [config]
  (let [{:keys [fqdn issuer]} config
        letsencrypt-issuer (name issuer)]
    (->
     (yaml/from-string (yaml/load-resource "jira/certificate.yaml"))
     (assoc-in [:spec :commonName] fqdn)
     (assoc-in [:spec :dnsNames] [fqdn])
     (assoc-in [:spec :issuerRef :name] letsencrypt-issuer))))

(defn generate-deployment [config]
  (let [{:keys [fqdn]} config]
    (-> (yaml/from-string (yaml/load-resource "jira/deployment.yaml"))
        (cm/replace-named-value "FQDN" fqdn))))

(defn generate-ingress [config]
  (let [{:keys [fqdn issuer]
         :or {issuer :staging}} config
        letsencrypt-issuer (name issuer)]
    (->
     (yaml/from-string (yaml/load-resource "jira/ingress.yaml"))
     (assoc-in [:metadata :annotations :cert-manager.io/cluster-issuer] letsencrypt-issuer)
     (cm/replace-all-matching-values-by-new-value "fqdn" fqdn))))

(defn-spec generate-pvc cp/map-or-seq?
  [config (s/keys :req-un [::pv-storage-size-gb  ::pvc-storage-class-name])]
  (let [{:keys [pv-storage-size-gb pvc-storage-class-name]} config]
    (->
     (yaml/from-string (yaml/load-resource "jira/pvc.yaml"))
     (assoc-in [:spec :resources :requests :storage] (str pv-storage-size-gb "Gi"))
     (assoc-in [:spec :storageClassName] (name pvc-storage-class-name)))))

(defn generate-service []
  (yaml/from-string (yaml/load-resource "jira/service.yaml")))
