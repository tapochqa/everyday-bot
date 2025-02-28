(defproject everyday-bot "0.1.0-SNAPSHOT"

  :description
  "Telegram Bot"
  
  :url
  "https://t.me/"

  :license
  {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies
  [[org.clojure/clojure  "1.11.1"]
   [http-kit             "2.6.0"]
   [cheshire             "5.10.0"]
   [link.lmnd/tg-bot-api "0.1.8"]]

  :main ^:skip-aot everyday-bot.core

  :target-path "target/uberjar"

  :uberjar-name "everyday-bot.jar"
  
  :jvm-opts ["-Dfile.encoding=UTF-8"]

  :profiles
  {:dev
   {:global-vars
    {*warn-on-reflection* true
     *assert* true}}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
