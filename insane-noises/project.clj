(defproject insane-noises "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [overtone "0.9.1"]
                 [me.raynes/conch "0.8.0"]
                 ]
  :plugins [[lein-gorilla "0.3.4"]]

  :repl-options {
                 :init-ns insane-noises.core
                 }
  :jvm-opts ^:replace [] 

  )
