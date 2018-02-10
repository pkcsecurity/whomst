(ns whomst.core
  (:require [whomst.public :as pub]
            [whomst.private :as priv]
            [whomst.constants :as c]
            [whomst.router :as r]))

(def drivers (atom []))

(def max-privates 2)

(defn -main [& args]
  (swap! drivers conj (pub/init))
  (dotimes [i max-privates]
    (swap! drivers conj (priv/init i))))

(defn reset []
  (doseq [d @drivers]
    (c/kill d))
  (reset! drivers [])
  (reset! r/privates [])
  (reset! r/public nil))
