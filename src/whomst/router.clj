(ns whomst.router
  (:require [clojure.core.async :as async]))

(defn chan []
  (async/chan (async/dropping-buffer 1024)))

(def privates (atom []))
(def public (atom (chan)))

; FIXME: Needs to do logic
(defn choose-private [seeker-id]
  (let [private-id (if (= seeker-id "+1 (917) 971-2532") 0 1)]
    (println "[router] selecting private-id for seeker-id:" seeker-id private-id)
    private-id))

(defn send-to-private [seeker-id public-id message]
  (let [private-id (choose-private seeker-id)
        private-chan (get @privates private-id)]
    (println "[router] sending message to private from:" seeker-id private-id message)
    (async/>!! private-chan
      {:seeker-id seeker-id
       :public-id public-id
       :private-id private-id
       :message message})
    private-id))

; FIXME: This will need public-chan logic one day
(defn send-to-public [{:keys [public-id private-id seeker-id] :as req} message]
  (println "[router] sending message to public from:" seeker-id private-id message)
  (let [public-chan @public]
    (async/>!! public-chan (assoc req :message message))
    public-id))

(defn init-recv [chan on-message]
  (async/go
    (while true
      (let [msg (async/<! chan)]
        (on-message msg)))))
