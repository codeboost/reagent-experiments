(ns simpleexample.core-v2
  (:require
    [clojure.string :as str]
    [reagent.core :as r]
    [reagent.ratom :refer [reaction]]))

(defn greeting-controller []
  (let [state (r/atom {:timer (js/Date.)
                       :time-color "#f34"})
        timer-updater (js/setInterval
                       #(swap! state assoc :timer (js/Date.)) 1000)]
    {:time-str (reaction
                (-> (:timer @state) .toTimeString (str/split " ") first))
     :time-color (reaction (:time-color @state))
     :set-time-color! #(swap! state assoc :time-color (-> % .-target .-value))}))

(defn greeting [message]
  [:h1 message])

(defn clock [{:keys [time-str time-color]}]
  [:div.example-clock
   {:style {:color @time-color}}
   @time-str])

(defn color-input [{:keys [time-color set-time-color!]}]
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value @time-color
            :on-change set-time-color!}]])

(defn simple-example* [controller]
  [:div
   [greeting "Hello world, it is now!"]
   [clock controller]
   [color-input controller]])

(defn simple-example []
  (let [controller (greeting-controller)]
    (fn []
      [simple-example* controller])))
