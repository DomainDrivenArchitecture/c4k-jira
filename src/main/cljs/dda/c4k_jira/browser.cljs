(ns dda.c4k-jira.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-jira.core :as core]
   [dda.c4k-jira.jira :as jira]
   [dda.c4k-common.browser :as br]))

(defn config-from-document []
  (let [jira-data-volume-path (br/get-content-from-element "jira-data-volume-path" :optional true :deserializer keyword)
        postgres-data-volume-path (br/get-content-from-element "postgres-data-volume-path" :optional true :deserializer keyword)
        restic-repository (br/get-content-from-element "restic-repository" :optional true :deserializer keyword)
        issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? postgres-data-volume-path)
       {:postgres-data-volume-path postgres-data-volume-path})
     (when (some? restic-repository)
       {:restic-repository restic-repository})
     (when (some? issuer)
       {:issuer issuer})
     )))

(defn validate-all! []
  (br/validate! "fqdn" ::jira/fqdn)
  (br/validate! "jira-data-volume-path" ::jira/jira-data-volume-path :optional true :deserializer keyword)
  (br/validate! "postgres-data-volume-path" ::jira/jira-data-volume-path :optional true :deserializer keyword)
  (br/validate! "restic-repository" ::jira/restic-repository :optional true :deserializer keyword)
  (br/validate! "issuer" ::jira/issuer :optional true :deserializer keyword)
  (br/validate! "auth" core/auth? :deserializer edn/read-string)
  (br/set-validated!))

(defn init []
  (-> js/document
      (.getElementById "generate-button")
      (.addEventListener "click"
                         #(do (validate-all!)
                              (-> (core/generate 
                                   (config-from-document) 
                                   (br/get-content-from-element "auth" :deserializer edn/read-string))
                                  (br/set-output!)))))
  (-> (br/get-element-by-id "fqdn")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "jira-data-volume-path")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "postgres-data-volume-path")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "restic-repository")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "issuer")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "auth")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  )