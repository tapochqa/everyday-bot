(ns everyday-bot.parsing
  (:require 
   [everyday-bot.vk :as api]))


(defn get-wall-count
  [{:keys [domain owner_id] :as config}]
  
  (:count
      (api/api-request config 
        :get
        :wall.get
        (conj (select-keys config [:domain :owner_id])
              {:count 1
               :offset 0}))))


(defn wall-count->iterations
  [wall-count]
  (inc (int (/ wall-count 100))))


(defn parse-wall
  [{:keys [domain owner_id] :as config}]
  
  (let [iters-range
        (range (wall-count->iterations (get-wall-count config)))
        
        raw-result
        (mapv
           (fn [k] (api/api-request config
                     :get
                     :wall.get
                     (conj (select-keys config [:domain :owner_id])
                           {:count 100
                            :offset (* k 100)})))
           iters-range)
        
        items-list
        (map :items raw-result)
        
        
        ]
  
  (mapv (fn [k] (select-keys k [:owner_id :id :text])) (flatten items-list))))


(defn compile-post
  [{:keys [owner_id id text]}]
  (format "%s \n\n\n vk.com/wall%s_%s" text owner_id id))




(comment
  
  (range 6)
  
  (select-keys {:a 1 :b 2 :c 3} [:a :b :d])
  
  
  (def CONFIG {:token (slurp "token")})
  
  (mapv compile-post (parse-wall (conj CONFIG {:domain "everydaysounds"})))
  
  (get-wall-count CONFIG {:domain "everydaysounds"}))




