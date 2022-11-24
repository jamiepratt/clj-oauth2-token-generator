(ns clj-oauth2-token-generator.core
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :refer [pprint]]
            [clj-http.client :as http]
            [clj-oauth2.client :as oauth2]
            [ring.util.response :as response]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer (defroutes GET context)]
            [ring.adapter.jetty :as jetty]
            [nrepl.server :as nrepl-server]
            [cider.nrepl :refer (cider-nrepl-handler)])
  (:gen-class))


(defn env [m & {:keys [prefix separator] :or {prefix "" separator ""}}]
  (into {}
        (map (fn [[k-kw entry]]
               (let [k (->>
                        k-kw
                        name
                        str/upper-case
                        (#(str/replace % "-" "_"))
                        (str prefix))]
                 [k-kw
                  (if (map? entry)
                    (env entry :prefix (str k separator))
                    (or (System/getenv k) entry))]))
             m)))

(comment (env {:a
               {:th "sdfsf"}} :prefix "P") ;; => {:a {:th "/opt....etc."}}
         )



(def config
  (-> (edn/read-string (slurp (io/resource "config.edn")))
      (env :prefix "OAUTH2_TOKEN_GEN_" :separator "_")))


(def oauth-params (merge {;; These should be set in the oauth-params.edn file
                          :redirect-uri "https://example.com.com/oauth2callback"
                          :client-id "*google-client-id*"
                          :client-secret "*google-client-secret*"
                          :scope ["https://www.googleapis.com/auth/userinfo.email" "https://picasaweb.google.com/data/"]
                          ;; These don't need changes
                          :authorization-uri "https://accounts.google.com/o/oauth2/auth"
                          :access-token-uri "https://accounts.google.com/o/oauth2/token"
                          :access-query-param :access_token
                          :grant-type "authorization_code"
                          :access-type "online"
                          :approval_prompt ""}
                         (:oauth-params config)))

(def auth-request (oauth2/make-auth-request oauth-params))

(defn save-token [req]
  (let [token (oauth2/get-access-token oauth-params
                                       (:params req)
                                       auth-request)
        token-info (:body (http/get "https://www.googleapis.com/oauth2/v1/tokeninfo"
                                    {:query-params {:access_token (:access-token token)}
                                     :as :json}))]
    (with-open [out (io/writer (io/file (:token-directory config "/tmp") (:email token-info)))]
      (pprint {:token token
               :info token-info}
              out))
    (response/redirect (:success-url config "/"))))

(defroutes authentication-controller
  (GET "/" []
       (response/response "No browsable content on this server"))
  (GET "/google-oauth" []
       (response/redirect (:uri auth-request)))
  (GET "/oauth2callback" []
       save-token))

(defn -main [ & _]
  (println "Starting app.")
  (nrepl-server/start-server :port 54654 :handler cider-nrepl-handler)
  (jetty/run-jetty (-> authentication-controller
                       wrap-keyword-params
                       wrap-params)
                   {:port (:port config 9492)}))
