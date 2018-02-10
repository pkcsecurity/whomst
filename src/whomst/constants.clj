(ns whomst.constants
  (:import [org.openqa.selenium.remote DesiredCapabilities]
           [org.openqa.selenium.chrome ChromeOptions ChromeDriver]
           [org.openqa.selenium.remote RemoteWebDriver]
           [org.openqa.selenium By Keys JavascriptExecutor]))

(def check-millis 250)

(def whats-app-url "https://web.whatsapp.com")

(def input-selector "div[contenteditable=true]")
(def message-selector ".msg")
(def user-selector "._1wjpf")
(def button-selector "._2lkdt")
(def new-message-selector ".OUeyt")

(defn chrome-opts [i]
  (let [path (str "/tmp/whomst_profile" (when i (str "_" i)))
        opt (str "user-data-dir=" path)]
    (doto (DesiredCapabilities.)
      (.setCapability ChromeOptions/CAPABILITY 
                      (doto (ChromeOptions.)
                        (.addArguments (into-array String [opt])))))))

(defn init-driver [i]
  (ChromeDriver. (chrome-opts i)))

(defn elements [driver s]
  (.findElements
    driver
    (By/cssSelector s)))

(def element (comp first elements))

(defn send-string [elem s]
  (.sendKeys elem (into-array String [s])))

(defn clear-string [elem] 
  (.clear elem))

(defn kill [d] (.quit d))

(defn go [driver url]
  (.get driver url))

(defn text [elem]
  (.getText elem))

(defn wait [secs f]
  (let [p (promise)]
    (println (format "waiting %d seconds" secs))
    (.start 
      (Thread. 
        (fn []
          (Thread/sleep (* 1000 secs))
          (deliver p (f)))))
    p))

(defn script [driver s]
  (.executeScript driver s (object-array [])))

(defn first-unread [driver]
  (.executeScript 
    driver 
    "var node = document.getElementsByClassName('OUeyt')[0];
    if (node) { 
    return node.parentNode.parentNode.parentNode.parentNode; 
    }" 
    (object-array [])))

(defn current-message [driver]
  (let [s (text (last (elements driver ".msg")))]
    (.substring s 0 (.lastIndexOf s "\n"))))

(defn first-unread-text [driver]
  (when-let [elem (first-unread driver)]
    (try
      (.click elem)
      (catch Exception e
        (println "exception in new message")
        (.click elem)))
    (current-message driver)))

(defn find-user [driver n]
  (first 
    (filter #(= n (text %))
            (elements driver user-selector))))

(defn send-message-to-user [d user msg lock]
  (try
    (.lock lock)
    (when-let [np (find-user d user)]
      (.click np)
      (send-string (element d input-selector) msg)
      (.click (element d button-selector)))
    (finally 
      (.unlock lock))))

(defn current-user-id [driver]
  (text 
    (last 
      (elements driver user-selector))))
