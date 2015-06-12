(ns sylt.core
  (:require
    [sylt.handler :refer [app init destroy]]
    [ring.middleware.reload :as reload]
    [org.httpkit.server :as http-kit]
    [environ.core :refer [env]]

    [gorilla-repl.core :as gorilla]
    [compojure.core :refer [defroutes routes wrap-routes]]
    [compojure.route :as route]

    [taoensso.timbre :as timbre])
  (:gen-class))

;contains function that can be used to stop http-kit server
(defonce server (atom nil))

(defn parse-port [[port]]
  (Integer/parseInt (or port (env :port) "3000")))

(defn start-server [port]
  (init)
  (reset! server
          (http-kit/run-server
            (if (env :dev) (reload/wrap-reload #'app) app)
            {:port port})))

(defn stop-server []
  (when @server
    (destroy)
    (@server :timeout 100)
    (reset! server nil)))


;; (defroutes gorilla-routes
;;            (GET "/load" [] (gorilla-repl.core/wrap-api-handler gorilla-repl.core/load-worksheet))
;;            (POST "/save" [] (gorilla-repl.core/wrap-api-handler gorilla-repl.core/save))
;;            (GET "/gorilla-files" [] (gorilla-repl.core/wrap-api-handler gorilla-repl.core/gorilla-files))
;;            (GET "/config" [] (gorilla-repl.core/wrap-api-handler gorilla-repl.core/config))
;;            (GET "/repl" [] gorilla-repl.websocket-relay/ring-handler)
;;            (route/resources "/")
;;            (route/files "/project-files" [:root "."]))

(defn my-run-gorilla-server
  [conf]
  ;; get configuration information from parameters
  (let [version (or (:version conf) "develop")
        webapp-requested-port (or (:port conf) 0)
        ip (or (:ip conf) "127.0.0.1")
        nrepl-requested-port (or (:nrepl-port conf) 0)  ;; auto-select port if none requested
        project (or (:project conf) "no project")
        keymap (or (:keymap (:gorilla-options conf)) {})
        _ (swap! gorilla-repl.core/excludes (fn [x] (clojure.set/union x (:load-scan-exclude (:gorilla-options conf)))))]
    ;; app startup
    ;;(println "Gorilla-REPL:" version)
    ;; build config information for client
    (gorilla-repl.core/set-config :project project)
    (gorilla-repl.core/set-config :keymap keymap)
    ;; check for updates
    ;;(version/check-for-update version)  ;; runs asynchronously
    ;; first startup nREPL
    (gorilla-repl.nrepl/start-and-connect nrepl-requested-port)
    ;; and then the webserver
    ;; (let [s (org.httpkit.server/run-server #'app {:port webapp-requested-port :join? false :ip ip})
    ;;       webapp-port (:local-port (meta s))]
    ;;   (spit (doto (clojure.java.io/file ".gorilla-port") .deleteOnExit) webapp-port)
    ;;   (println (str "my gorilla running at http://" ip ":" webapp-port "/worksheet.html ."))
    ;;   (println "Ctrl+C to exit."))
    )
  )



(defn -main [& args]
  (let [port (parse-port args)]
    (.addShutdownHook (Runtime/getRuntime) (Thread. stop-server))
    (start-server port)
    (my-run-gorilla-server {:port 8990}) 

    (timbre/info "server started on port:" port)))
