(ns everyday-bot.vk
  (:require
    [cheshire.core :as json]
    [clojure.java.io :as io]
    [clojure.string :as str]
    [org.httpkit.client :as http]
    [clojure.java.io :as io]
    [clojure.edn :as edn])
   (:import java.security.MessageDigest))


(defn save-file [uri file]
  (with-open [in (io/input-stream uri)
              out (io/output-stream file)]
    (io/copy in out)))


(defn filter-params
  "
  Filter out nil values from a map.
  "
  [params]
  (persistent!
   (reduce-kv
    (fn [result k v]
      (if (some? v)
        (assoc! result k v)
        result))
    (transient {})
    params)))


(defn encode-params
  "
  JSON-encode complex values of a map.
  "
  [params]
  (persistent!
   (reduce-kv
    (fn [result k v]
      (if (coll? v)
        (assoc! result k (json/generate-string v))
        (assoc! result k v)))
    (transient {})
    params)))



(defn api-request
  [{:keys [token]}
   http-method
   api-method
   payload]
  
  (let [payload
        (conj payload
          {:access_token token
           :v "5.154"})
        
        method
        (if (= http-method :post-multipart)
              :post
              http-method)
        
        params
        {:url 
         (str "https://api.vk.com/method/" 
           (name api-method))
         
         :method method}
        
        params
        (if (= http-method :post-multipart)
          (assoc params :multipart payload)  
          (assoc params :query-params payload))
        
        {:keys [body]}
        @(http/request params)
        
        json
        (json/decode body true)
        
        response
        (:response json)]
    
    (if response
      response
      json)))



(defn get-playlist
  [config owner_id id]
    (api-request
      config
      :get
      :audio.get
      {:owner_id owner_id
       :playlist_id id}))


(defn remove-audio-duplicates
  [config {:keys [attachments] :as post}]
  
  (let [audios 
        (filter (comp #{"audio"} :type) attachments)
        
        playlist
        (:audio_playlist (first (filter (comp #{"audio_playlist"} :type) attachments)))
        
        playlist-audios
        (:items
          (get-playlist config (:owner_id playlist) (:id playlist)))
        
        audios-w-o-dups
        (map (fn [i]
               (if
                 (seq
                   (filter (fn [j] (= (-> i :audio :access_key)
                                      (-> j :access_key))) 
                     playlist-audios))
                 nil
                 i))
          audios)
        
        audios-w-o-dups
        (remove nil? audios-w-o-dups)
        ]
    
  
  
    audios))

(comment
  
  (do
    (remove-audio-duplicates
    CONFIG
    (edn/read-string (slurp "resources/post.edn"))))
  
  )



(defn download-photo
  [{:keys [sizes]} out]
  
  (let [url
        (-> 
          (sort-by :height sizes)
          reverse
          first
          :url)]
    url
    (save-file url (str "assets/photo/" out ".jpg"))))



(comment
  
  
  (let [config 
        {:token 
         (slurp "token-rzhombic")}
        
        server
        (api-request
          RZHOMBIC
          :get
          :stories.getPhotoUploadServer
          {:add_to_news 1})
        
        server
        (:upload_url server)
        
        {:keys [body]}
        @(http/request
          {:url
           server
           
           :method
           :post
           
           :multipart
           [{:name "file"
             :filename "a.png"
             :content (io/input-stream "/Users/m0x3mkx/Pictures/warn2.png")}]})
        
        upload
        (json/decode body true)
        
        result
        (get-in upload [:response :upload_result])
        
        ]
    
    (api-request config
      :post
      :stories.save
      {:upload_results result}))
  
  (def CONFIG {:token (slurp "token")})
  
  
  (api-request)
  
  (->>
    (api-request
    CONFIG
    :get
    :store.getProducts
    {:type "stickers"
     ;:user_id 1
     :filters ["purchased"]
     :extended 1})
    :items
    (filter (comp #{1} :purchased))
    (map :title)
    sort)
  
  (->>
    (api-request
      CONFIG
      :get
      :wall.get
      {:domain "everydaysounds"
       :count 100
       :offset 500})
    #_:count
    #_:items
    #_(mapv :text)
    #_(spit "resources/post.edn")
    #_count
    #_(filter (comp #{"audio"} :type))
    #_first
    #_:audio_playlist
    #_(download-playlist CONFIG)
    #_:audios
    #_(download-post CONFIG)
    #_(map :audio)
    #_(map download-audio)))








