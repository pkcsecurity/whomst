(ns whomst.router
  (:require [clojure.core.async :as async]
            [whomst.http :as http]))

(defn chan []
  (async/chan (async/dropping-buffer 1024)))

(def privates (atom []))
(def public (atom (chan)))

(def linked (atom #{}))

(defn flush-all []
  (http/flush-all)
  (reset! linked #{}))

(defn get-routing-from-table [seeker-id]
  (let [{:keys [seeker->private seeker->partners]} (http/get-table)
        kw-seeker-id (keyword seeker-id)]
    {:private-id (get seeker->private kw-seeker-id)
     :seeker-id seeker-id
     :public-id 0
     :partner-id (get-in seeker->partners [kw-seeker-id :partnerNumber])}))

(defn get-routing-info [seeker-id]
  (if (contains? @linked seeker-id)
    (get-routing-from-table seeker-id)
    (do 
      (swap! linked conj seeker-id)
      (http/link seeker-id))))

(defn get-seeker-id [private-id partner-id]
  (let [{:keys [seeker->private seeker->partners]} (http/get-table)]
    (when-let [[k {:keys [seekerNumber]}]
               (first
                 (filter
                   (fn [[seeker-id {:keys [seekerNumber partnerNumber]}]]
                     (when (and (= partnerNumber partner-id)
                                (= (get seeker->private (keyword seekerNumber)) private-id))
                       seekerNumber))
                   seeker->partners))]
      seekerNumber)))

(defn send-to-private [seeker-id public-id message]
  (let [{:keys [private-id] :as route} (get-routing-info seeker-id)
        private-chan (get @privates private-id)
        message (assoc route :message message)]
    (println "[router] sending message to private from:" seeker-id private-id message)
    (async/>!! private-chan message)
    private-id))

(defn send-to-public [{:keys [partner-id private-id message] :as req}]
  (let [seeker-id (get-seeker-id private-id partner-id)]
    (println "[router] sending message to public from:" seeker-id private-id message)
    (let [public-chan @public]
      (async/>!! public-chan (assoc req 
                                    :seeker-id seeker-id
                                    :public-id 0)))))

(defn init-recv [chan on-message]
  (async/go
    (while true
      (let [msg (async/<! chan)]
        (on-message msg)))))
