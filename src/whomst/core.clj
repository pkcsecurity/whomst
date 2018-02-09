(ns whomst.core
  (:import [org.openqa.selenium.remote DesiredCapabilities]
           [org.openqa.selenium.chrome ChromeOptions ChromeDriver]
           [org.openqa.selenium.remote RemoteWebDriver]
           [org.openqa.selenium By Keys JavascriptExecutor])
  (:require [whomst.sm :as sm]
            [whomst.np :as np]
            [whomst.constants :as c]))

(defn -main [& args]
  (sm/init)
  (np/init))

(defn kill []
  (c/kill @sm/driver)
  (c/kill @np/driver)
  (System/exit 0))
