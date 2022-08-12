(ns simpleexample.v3-state
  (:require
    [clojure.string :as str]
    [reagent.core :as r]
    [reagent.ratom :refer [reaction]]))

(defn- set-color! [state color]
  (let [color (if (string? color)
                color
                (-> color .-target .-value))]
    (swap! state assoc :color color)))

(defn- start-date-timer! [state interval]
  (swap! state assoc :timer (js/setInterval #(swap! state assoc :date (js/Date.)) interval)))

(defn- format-date [date]
  (some-> date .toTimeString (str/split " ") first))

(defn v3-state []
  (let [state (r/atom {:color "#f34"
                       :date  (js/Date.)})]
    (start-date-timer! state 1000)
    {:set-color!     #(set-color! state %)
     :color          (reaction (:color @state))
     :formatted-time (reaction (format-date (:date @state)))}))







(comment
  (defn state-controller [props]
    (let [state (r/atom {:username (:username props)})]
      {:set-username! #(swap! state assoc :username (-> % .-target .-value))
       :username      (reaction (:username @state))})))