(ns test-api.handler
  (:require [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]
            [ring.middleware.cors :refer [wrap-cors]]))

(s/defschema Pizza
  {:name s/Str
   (s/optional-key :description) s/Str
   :size (s/enum :L :M :S)
   :origin {:country (s/enum :FI :PO)
            :city s/Str}})

(defn wrap-logging [app]
  (fn [req]
    (prn req)
    (app req)))

(defn wrap-exceptions [app]
  (fn [req]
    (try
      (app req)
      (catch Exception e
        (prn e)
        {:status 500}))))

(defn else-404 [app]
  (fn [req]
    (let [response (app req)]
      (if response
        response
        {:status 404}))))

(def app
  (-> (api
       {:swagger
        {:ui "/"
         :spec "/swagger.json"
         :data {:info {:title "Test-api"
                       :description "Compojure Api example"}
                :tags [{:name "api", :description "some apis"}]}}}

       (context "/api" []
                :tags ["api"]

                (GET "/slow-plus" []
                     :return {:result Long}
                     :query-params [x :- Long, y :- Long]
                     :summary "adds two numbers together"
                     (ok (do
                           (Thread/sleep 5000)
                           {:result (+ x y)})))

                (GET "/plus" []
                     :return {:result Long}
                     :query-params [x :- Long, y :- Long]
                     :summary "adds two numbers together"
                     (ok {:result (+ x y)}))

                (POST "/echo" []
                      :return Pizza
                      :body [pizza Pizza]
                      :summary "echoes a Pizza"
                      (ok pizza))

                (PUT "/echo" []
                      :return Pizza
                      :body [pizza Pizza]
                      :summary "echoes a Pizza"
                      (ok pizza))))
      (else-404)
      (wrap-exceptions)
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :put :post :delete])
      (wrap-logging)))
