(ns whomst.private
  (:require [whomst.constants :as c]
            [whomst.router :as r]
            [whomst.http :as http]
            [whomst.dfa :as dfa])
  (:import [java.util.concurrent.locks ReentrantLock]))

(defn req->receiver [{:keys [seeker-id public-id private-id partner-id] :as req}]
  partner-id)

(defn process-private-message [private-id partner-id message]
  (println :private partner-id message)
  (r/send-to-public {:private-id private-id
                     :partner-id partner-id
                     :message message}))

(defn init-channel [driver chan lock]
  (dfa/channel-message-listener
    driver 
    chan
    req->receiver
    lock))

(defn init-ui [driver lock private-id]
  (dfa/ui-message-listener 
    driver 
    (partial process-private-message private-id)
    lock))

(defn init [private-id]
  (let [driver (c/init-driver private-id)
        chan (r/chan)
        lock (ReentrantLock.)]
    (swap! r/privates conj chan)
    (future
      (c/go driver c/whats-app-url)
      (init-channel driver chan lock)
      (init-ui driver lock private-id))
    driver))

;(defn seeker->partner [seeker]
;  (println ::seeker->partner seeker)
; (if (= seeker "+1 (917) 971-2532")
;   "+66 93 317 3260"
;   "Mike Mirabell"))
