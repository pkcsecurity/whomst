(ns whomst.public
  (:require [whomst.constants :as c]
            [whomst.router :as r]
            [whomst.dfa :as dfa])
  (:import [java.util.concurrent.locks ReentrantLock]))

(defn req->receiver [{:keys [seeker-id]}]
  seeker-id)

(def my-public-id 0)

(defn process-public-message [seeker-id message]
  (println (format "[public] received message from: %s saying...\n%s" seeker-id message))
  (r/send-to-private seeker-id my-public-id message))

(defn init-channel [driver lock]
  (dfa/channel-message-listener 
    driver 
    @r/public 
    req->receiver
    lock))

(defn init-ui [driver lock]
  (dfa/ui-message-listener
    driver 
    process-public-message
    lock))

(defn init []
  (let [driver (c/init-driver nil)
        lock (ReentrantLock.)]
    (reset! r/public (r/chan))
    (future
      (c/go driver c/whats-app-url)
      (init-channel driver lock)
      (init-ui driver lock))
    driver))
