(ns dda.c4k-jira.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-jira.core :as core]
   [dda.c4k-jira.jira :as jira]
   [dda.c4k-common.browser :as br]
   [dda.c4k-common.postgres :as pgc]))

(defn generate-content
  []
  (into [] (concat [(assoc (br/generate-needs-validation) :content
                           (into [] (concat (br/generate-input-field "fqdn" "Your fqdn:" "jira-neu.prod.meissa-gmbh.de" "dda.c4k_jira.browser")
                                            (br/generate-input-field "jira-data-volume-path" "(Optional) Your jira-data-volume-path:" "/var/jira" "dda.c4k_jira.browser")
                                            (br/generate-input-field "postgres-data-volume-path" "(Optional) Your postgres-data-volume-path:" "/var/postgres" "dda.c4k_jira.browser")
                                            (br/generate-input-field "restic-repository" "(Optional) Your restic-repository:" "restic-repository" "dda.c4k_jira.browser")
                                            (br/generate-input-field "issuer" "(Optional) Your issuer prod/staging:" "" "dda.c4k_jira.browser")
                                            [(br/generate-br)]
                                            (br/generate-text-area "auth" "Your auth.edn:" "{:postgres-db-user \" jira \"
         :postgres-db-password \" jira-db-password \"
         :aws-access-key-id \" aws-id \"
         :aws-secret-access-key \" aws-secret \"
         :restic-password \" restic-password \"}"
                                                                   "5" "dda.c4k_jira.browser")
                                            [(br/generate-br)]
                                            (br/generate-button "generate-button" "Generate c4k yaml"))))]
                   (br/generate-output "c4k-keycloak-output" "Your c4k deployment.yaml:" "25"))))

(defn generate-container-jumbotron
  []
  {:type :element
   :attrs {:class "container jumbotron"}
   :tag :div
   :content
   (generate-content)})

(defn config-from-document []
  (let [jira-data-volume-path (br/get-content-from-element "jira-data-volume-path" :optional true)
        postgres-data-volume-path (br/get-content-from-element "postgres-data-volume-path" :optional true)
        restic-repository (br/get-content-from-element "restic-repository" :optional true)
        issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? jira-data-volume-path)
       {:jira-data-volume-path jira-data-volume-path})
     (when (some? postgres-data-volume-path)
       {:postgres-data-volume-path postgres-data-volume-path})
     (when (some? restic-repository)
       {:restic-repository restic-repository})
     (when (some? issuer)
       {:issuer issuer})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::jira/fqdn)
  (br/validate! "jira-data-volume-path" ::jira/jira-data-volume-path :optional true)
  (br/validate! "postgres-data-volume-path" ::pgc/postgres-data-volume-path :optional true)
  (br/validate! "restic-repository" ::jira/restic-repository :optional true)
  (br/validate! "issuer" ::jira/issuer :optional true :deserializer keyword)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn init []
  (br/replace-element-content "app" (generate-container-jumbotron))
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (core/generate 
                                   (config-from-document) 
                                   (br/get-content-from-element "auth" :deserializer edn/read-string))
                                  (br/set-output!))))))