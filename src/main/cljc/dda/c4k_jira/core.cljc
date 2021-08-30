(ns dda.c4k-jira.core
 (:require
  [clojure.string :as cs]
  [clojure.spec.alpha :as s]
  #?(:clj [orchestra.core :refer [defn-spec]]
     :cljs [orchestra.core :refer-macros [defn-spec]])
  [dda.c4k-common.yaml :as yaml]
  [dda.c4k-common.postgres :as postgres]
  [dda.c4k-jira.jira :as jira]
  [dda.c4k-jira.backup :as backup]
  [clojure.zip :as zip]
  [hickory.core :as hc]
  [hickory.zip :as hz]))

(def config-defaults {:issuer :staging})

(def config? (s/keys :req-un [::jira/fqdn]
                     :opt-un [::jira/issuer ::jira/jira-data-volume-path
                              ::postgres/postgres-data-volume-path ::restic-repository]))

(def auth? (s/keys :req-un [::postgres/postgres-db-user ::postgres/postgres-db-password
                            ::aws-access-key-id ::aws-secret-access-key
                            ::restic-password]))

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
            (yaml/to-string (jira/generate-deployment config))
            (yaml/to-string (jira/generate-service))
            (yaml/to-string (jira/generate-certificate config))
            (yaml/to-string (jira/generate-ingress config))
            (yaml/to-string (jira/generate-service))]
           (when (contains? config :restic-repository)
             [(yaml/to-string (backup/generate-config config))
              (yaml/to-string (backup/generate-secret config))
              (yaml/to-string (backup/generate-cron))]))))

; START OF HTML EDIT REFACTOR
; TODO: Move this to somewhere in commons

(def htest (hz/hickory-zip (hc/as-hickory (hc/parse (clojure.core/slurp "public/index.html")))))

(defn generate-feedback-tag
  [id]
  {:type :element :attrs {:class "invalid-feedback"} :tag :div :content [{:type :element :attrs {:id (str id "-validation")} :tag :pre :content "nil"}]})

(defn generate-label
  [id-for
   label]
  {:type :element :attrs {:for id-for :class "form-label"} :tag :label :content [label]})

(defn generate-input-field
  [id
   label
   default-value]
  [(generate-label id label)
   {:type :element :attrs {:class "form-control" :type "text" :name id :value default-value} :tag :input :content "nil"}
   (generate-feedback-tag id)])

(defn generate-text-area
  [id
   label
   default-value
   rows]
  [(generate-label id label)
   {:type :element :attrs {:name id :id id :class "form-control" :rows rows} :tag :textarea :content default-value}
   (generate-feedback-tag id)])

(defn generate-output
  [id
   label
   rows]
  {:type :element, :attrs {:id id}, :tag :div, :content [
       {:type :element, :attrs {:for "output", :class "form-label"}, :tag :label, :content [label]} 
       {:type :element, :attrs {:name "output", :id "output", :class "form-control", :rows rows}, :tag :textarea, :content []} 
    ]})

(defn generate-needs-validation
  []
  {:type :element, :attrs {:class :needs-validation, :id :form}, :tag :form, :content []})

; TODO: add br tags
; TODO: add generate button
; TODO: add output text-area
(defn generate-content
  []
  (conj
   (assoc (generate-needs-validation) :content
          (conj (generate-input-field "fqdn" "Your fqdn:" "jira-neu.prod.meissa-gmbh.de")
                (generate-input-field "jira-data-volume-path" "(Optional) Your jira-data-volume-path:" "/var/jira")
                (generate-input-field "postgres-data-volume-path" "(Optional) Your postgres-data-volume-path:" "/var/postgres")
                (generate-input-field "restic-repository" "(Optional) Your restic-repository:" "restic-repository")
                (generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "")
                (generate-text-area "auth" "Your auth.edn:" "{:postgres-db-user \" jira \"
         :postgres-db-password \" jira-db-password \"
         :aws-access-key-id \" aws-id \"
         :aws-secret-access-key \" aws-secret \"
         :restic-password \" restic-password \"}"
                                    5)))
   (generate-output "c4k-keycloak-output" "Your c4k deployment.yaml:" 25)))

(defn find-map
  [zipper]
  (if (not (zip/end? zipper))
    (find-map (zip/next zipper))
    (zip/node zipper))
  (if (and (map? (zip/node zipper))
           (= (:class (:attrs (zip/node zipper))) "container jumbotron"))
    ;replace instead of print later
    (assoc (zip/node zipper) :content (generate-content))))

; END REFACTOR

(defn-spec generate any?
  [my-config config?
   my-auth auth?]
  (let [resulting-config (merge config-defaults my-config my-auth)]
    (cs/join
     "\n---\n"
     (k8s-objects resulting-config))))
