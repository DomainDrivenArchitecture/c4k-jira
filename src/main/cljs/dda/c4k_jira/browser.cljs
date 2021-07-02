(ns dda.c4k-jira.browser
  (:require
   [clojure.tools.reader.edn :as edn]
   [dda.c4k-jira.core :as core]
   [dda.c4k-jira.jira :as jira]
   [dda.c4k-common.browser :as br]))

(defn config-from-document []
  (let [issuer (br/get-content-from-element "issuer" :optional true :deserializer keyword)]
    (merge
     {:fqdn (br/get-content-from-element "fqdn")}
     (when (some? issuer)
       {:issuer issuer}))))

(defn validate-all! []
  (br/validate! "fqdn" ::jira/fqdn)
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
  (-> (br/get-element-by-id "issuer")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  (-> (br/get-element-by-id "auth")
      (.addEventListener "blur"
                         #(do (validate-all!))))
  )