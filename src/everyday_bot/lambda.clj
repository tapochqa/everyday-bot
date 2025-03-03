;; https://github.com/ring-clojure/ring/blob/master/SPEC
;; https://cloud.yandex.ru/docs/functions/concepts/function-invoke

(ns everyday-bot.lambda
  
  (:require
   [clojure.java.io :as io]
   [cheshire.core :as json]
   [clojure.string :as str]
   [everyday-bot.handling :as handling]
   [tg-bot-api.telegram :as telegram])
  
  (:import
   java.util.Base64
   ))


(defmacro with-safe-log
  "
  A macro to wrap Telegram calls (prevent the whole program from crushing).
  "
  [& body]
  `(try
     ~@body
     (catch Throwable e#
       (str (ex-message e#)))))


(defn str->bytes
  ^bytes [^String string ^String encoding]
  (.getBytes string encoding))


(defn b64-decode
  [^bytes encoded]
  (.decode (Base64/getDecoder) encoded))


(defn parse-request
  [{:strs [requestContext
           path
           queryStringParameters
           httpMethod
           body
           isBase64Encoded
           headers
           messages]} {:keys [debug-chat-id] :as config}]
  (let [parsed
        {:remote-addr (get-in requestContext ["identity" "sourceIp"])
         :uri (if (= path "") "/" path)
         :query-params queryStringParameters
         :request-method 
         (if httpMethod
           (-> httpMethod name str/lower-case keyword)
           :trigger)
         :headers (update-keys headers str/lower-case)
         :messages (json/generate-string messages)
         :body (if isBase64Encoded
                 (-> (str "" body)
                     (str->bytes "UTF-8")
                     (b64-decode)
                     (io/input-stream))
                 (-> (str "" body)
                     (str->bytes "UTF-8")
                     (io/input-stream)))}]
    (when debug-chat-id
      (telegram/send-message config debug-chat-id (str parsed)))
    parsed))


(defn ->request [config]
  (-> *in*
      (json/parse-stream)
      (parse-request config)))


(defn encode-body [body]
  (cond

    (string? body)
    {:body body
     :isBase64Encoded false}

    :else
    (throw (ex-info "Wrong response body"
                    {:body body}))))


(defn handle-request!
  [{:keys [headers body messages]} config]
  
  (let [update (-> body
                 slurp
                 (json/parse-string true)
                 )
        trigger-id
                (-> messages
                  (json/parse-string true)
                  first
                  :details
                  :trigger_id 
                  )]
  
  {:body
   (with-safe-log
     (json/encode
       (handling/the-handler
         config
         update
         trigger-id)))
   
   :headers headers
   
   :status 200}))


(defn response->
  [{:keys [status headers]}]
  (json/with-writer [*out* nil]
    (json/write
     (cond-> nil
       status
       (assoc :statusCode status)
       headers
       (assoc :headers headers)))))
