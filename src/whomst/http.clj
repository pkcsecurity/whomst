(ns whomst.http
  (:require [cheshire.core :as json]
            [clj-http.client :as client]))

(def server "http://54.175.113.86:8000")

(defn json->clj [s]
  (json/parse-string s true))

(defn clj->json [obj]
  (json/generate-string obj))

(defn get-table []
  (json->clj
    (:body
      (client/get (str server "/table") 
                  {:accept :json}))))

(defn link [seeker-id]
  (json->clj
    (:body
      (client/post (str server "/link")
                   {:body (clj->json {:seeker-id seeker-id})
                    :content-type :json}))))

(defn flush-all []
  (client/post (str server "/flush")
               {:body "[]"
                :content-type :json}))
