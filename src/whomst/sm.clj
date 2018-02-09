(ns whomst.sm
  (:require [whomst.constants :as c]
            [whomst.router :as r]))

(defonce driver 
  (delay 
    (c/init-driver true)))

(defn on-message-to-np [msg]
  (println :sm->np msg)
  (r/send-to-np msg))

(defn on-message-to-sm [msg]
  (let [d @driver]
    (when-let [np (c/find-user d "+1 (917) 971-2532")]
      (.click np)
      (c/send-string (c/element d c/input-selector) msg)
      (.click (c/element d c/button-selector)))))


(defn init []
  (future
    (c/go @driver c/whats-app-url)
    (r/init-recv-sm on-message-to-sm)
    (c/dfa @driver on-message-to-np)))
