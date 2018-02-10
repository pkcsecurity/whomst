(ns whomst.dfa
  (:require [whomst.constants :as c]
            [clojure.core.async :as async]))

(def wait-millis 200)

(defn message? [text last-message]
  (and text (not= text last-message)))

(defn ui-message-listener [driver on-message lock]
  (future
    (let [last-message (atom nil)]
      (while true
        (try
          (.lock lock)
          (let [text (c/first-unread-text driver)]
            (if (message? text @last-message)
              (let [user-id (c/current-user-id driver)]
                (.unlock lock)
                (on-message user-id (reset! last-message text)))
              (do
                (.unlock lock)
                (Thread/sleep wait-millis))))
          (catch Exception e 
            (println e)
            (.unlock lock)))))))

(defn channel-message-listener [driver chan req->receiver lock]
  (async/go
    (while true
      (try
        (let [{:keys [message] :as req} (async/<! chan)
              receiver (req->receiver req)]
          (c/send-message-to-user driver receiver message lock))
        (catch Exception e (println e))))))
