(ns whomst.core
  (:import [org.openqa.selenium.remote DesiredCapabilities]
           [org.openqa.selenium.chrome ChromeOptions ChromeDriver]
           [org.openqa.selenium.remote RemoteWebDriver]))

(def whats-app-url "https://web.whatsapp.com")

(def chrome-opts
  (doto (DesiredCapabilities.)
    (.setCapability ChromeOptions/CAPABILITY 
                    (doto (ChromeOptions.)
                      (.addArguments (into-array String 
                                                 ["user-data-dir=/tmp/whomst_profile"]))))))

(def driver (atom nil))

(defn init []
  (reset! driver (ChromeDriver. chrome-opts)))

(defn kill []
  (.quit @driver))

(defn go-to-whatsapp []
  (.get @driver whats-app-url))

(defn -main [& args]
  (init)
  (go-to-whatsapp))
