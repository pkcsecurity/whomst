(ns whomst.private
  (:require [whomst.constants :as c]
            [whomst.router :as r]
            [whomst.dfa :as dfa])
  (:import [java.util.concurrent.locks ReentrantLock]))

(def partner-id->meta (atom {}))

(def partners ["Mike Mirabell" "Mike Mirabella"])

(defn choose-partner-id [private-id]
  (let [partner-id (get partners private-id)]
    (println "[private] mapping private-id to available partner" private-id partner-id)
    partner-id))

(defn req->receiver [{:keys [seeker-id public-id private-id] :as req}]
  (let [partner-id (choose-partner-id private-id)]
    (swap! partner-id->meta assoc partner-id req)
    (println "[private] assoc'ing in partner-id with meta" partner-id req)
    partner-id))

(defn process-private-message [partner-id message]
  (println :private partner-id message)
  (if-let [m (get @partner-id->meta partner-id)]
    (r/send-to-public m message)
    (println "[private] ignoring: message received without a meta mapping")))

(defn init-channel [driver chan lock]
  (dfa/channel-message-listener
    driver 
    chan
    req->receiver
    lock))

(defn init-ui [driver lock]
  (dfa/ui-message-listener 
    driver 
    process-private-message 
    lock))

(defn init [i]
  (let [driver (c/init-driver i)
        chan (r/chan)
        lock (ReentrantLock.)]
    (swap! r/privates conj chan)
    (future
      (c/go driver c/whats-app-url)
      (init-channel driver chan lock)
      (init-ui driver lock))
    driver))


;(defn seeker->partner [seeker]
;  (println ::seeker->partner seeker)
; (if (= seeker "+1 (917) 971-2532")
;   "+66 93 317 3260"
;   "Mike Mirabell"))
