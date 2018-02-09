(ns whomst.np
  (:require [whomst.constants :as c]
            [whomst.router :as r]))

(defonce driver 
  (delay (c/init-driver false)))

(defn on-message-to-sm [msg]
  (println :np->sm msg)
  (r/send-to-sm msg))

(defn on-message-to-np [msg]
  (let [d @driver]
    (when-let [np (c/find-user d "Matt Birch")]
      (.click np)
      (c/send-string (c/element d c/input-selector) msg)
      (.click (c/element d c/button-selector)))))

(defn init []
  (future
    (c/go @driver c/whats-app-url)
    (r/init-recv-np on-message-to-np)
    (c/dfa @driver on-message-to-sm)))
