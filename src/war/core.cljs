(ns ^:figwheel-hooks war.core
  (:require
   [clojure.pprint :refer [pprint]]
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.core :as reagent :refer [atom]]
   [war.actions :as actions]
   [war.cards :as cards]
   [war.logic :as logic]
   [war.util :as util] 
   [war.view :as view]
   [war.reframe :as reframe]
   [re-frame.core :as rf]))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount [el]
  (rf/dispatch-sync [:initialize])
  (reagent/render-component [view/war] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )


