(ns modern-cljs.core
  (:require [modern-cljs.html :refer [build-pages send-welcome-page]]
            [modern-cljs.database :refer [users]]
            [compojure.route :refer [resources not-found]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.handler :refer [api site]]
            [cemerick.shoreleave.rpc :refer [defremote wrap-rpc]]))

(defremote authentication
  [email password]
  (let [users-credentials (set (keys users))
        user {:email email :password password}]
    {:log-error (not (contains? users-credentials user)) :username (users user)}))

(defremote welcome
  [username]
  (send-welcome-page username))

(defn authenticate
  [email password]
  (let [user {:email email :password password}
        regex-mail #"^[_a-z0-9-]+(\.[_a-z0-9-]+)*@[a-z0-9-]+(\.[a-z0-9-]+)*(\.[a-z]{2,4})$"]
    (merge {:form-error (or (empty? email) (empty? password))
            :email-error (empty? (re-matches regex-mail email))}
            (authentication email password))))

(defn login
  [email password]
  (build-pages (authenticate email password)))
  
(defroutes app-routes
  (GET "/" [] "<p>Hello from compojure</p>")
  (POST "/login-page.html" [email password] (login email password))
  (resources "/")
  (not-found "Page non found"))

(def handler
  (api app-routes))

(def app (-> (var handler)
             (wrap-rpc)
             (site)))
