(ns everyday-bot.core 
  (:gen-class)
  (:require
    [everyday-bot.polling  :as polling]
    [everyday-bot.lambda   :as lambda]
    [clojure.string    :as str]
    [cheshire.core     :as json]))


(defn polling
  [config]
  (polling/run-polling config))

(defn lambda
  [config]
  (-> (lambda/->request config)
      (lambda/handle-request! config)
      (lambda/response->)))

(defn -main
  [tg-token vk-token owner_id]
  
  (let [config 
        { :test-server false
          :token tg-token
          :vk-token vk-token
          :owner_id owner_id
          :polling {:update-timeout 1000}
          }]
  #_(polling/run-polling config)
  (lambda config)))


(comment
  
  (-main (slurp "token") (slurp "vk-token") (slurp "owner_id"))
  
   (binding [*in* (-> "trigger-request.json"
                 clojure.java.io/resource
                 clojure.java.io/reader)]
     
     (-main (slurp "token") (slurp "vk-token") (slurp "owner_id")))
  
  
  (-main "...:...")
  
  )
