(ns whomst.router
  (:require [clojure.core.async :as async]))

(defn chan []
  (async/chan (async/dropping-buffer 1024)))

(def sm->np (chan))

(def np->sm (chan))

(defn send-to-sm [msg]
  (async/>!! np->sm msg))

(defn send-to-np [msg]
  (async/>!! sm->np msg))

(defn init-recv [chan on-message]
  (async/go
    (while true
      (let [msg (async/<! chan)]
        (on-message msg)))))

(def init-recv-sm (partial init-recv np->sm))
(def init-recv-np (partial init-recv sm->np))
