(defproject phrame "0.1.0-SNAPSHOT"
  :description "Server to generate Google OAuth2 tokens"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [sudharsh/clj-oauth2 "0.5.3"]
                 [ring "1.9.6"]
                 [compojure "1.7.0"]
                 [org.apache.httpcomponents/httpclient "4.5.13"]
                 [clj-http "3.12.3"]]
  :plugins [[lein-ancient "1.0.0-RC3"]]
  :main clj-oauth2-token-generator.core)
