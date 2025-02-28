(ns everyday-bot.handling
  (:require
    [tg-bot-api.telegram :as telegram]
    [everyday-bot.parsing :as parsing]))


(defn the-handler 
  "Bot logic here"
  [{:keys [domain owner_id] :as config} {:keys [message]} trigger-id]
  
  (do
    
    (telegram/send-message config 163440129 
      
      (->> (parsing/parse-wall (assoc config :token (:vk-token config)))
           (mapv parsing/compile-post)
           (rand-nth)
           str))))


