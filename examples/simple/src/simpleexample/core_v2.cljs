(ns simpleexample.core-v2
  (:require
    [clojure.string :as str]
    [reagent.core :as r]))

(defprotocol IColor
  (-set-color! [this color])
  (-color [this]))

(defprotocol IDateTick
  (-date-string [this])
  (-start-ticking! [this])
  (-stop-ticking! [this]))

(deftype ExampleState [state]
  IColor
  (-set-color! [this color]
    (swap! state assoc :color color))
  (-color [this]
    (:color @state))

  IDateTick
  (-date-string [this]
    (let [date (:date @state)]
      (-> date .toTimeString (str/split " ") first)))

  (-start-ticking! [this]
    (swap! state assoc :timer (js/setInterval
                                #(swap! state assoc :date (js/Date.)) 1000)))
  (-stop-ticking! [this]
    (swap! state (fn [{:keys [timer] :as s}]
                   (when timer
                     (js/clearInterval timer))
                   (dissoc s :timer)))))

(defn example-state []
  (let [state (r/atom {:color "#f34"
                       :date (js/Date.)})
        state-controller (ExampleState. state)]
    (-start-ticking! state-controller)
    state-controller))

;;;-----------

(defn greeting [message]
  [:h1 message])

(defn color-input [es]
  [:div.color-input
   "Time color: "
   [:input {:type "text"
            :value (-color es)
            :on-change #(-set-color! es (-> % .-target .-value))}]])


(defn clock [es]
  (let [time-str (-date-string es)]
    [:div.example-clock
     {:style {:color (-color es)}}
     time-str]))

(defn simple-example []
  (let [es (example-state)]
    (fn []
      [:div
       [greeting "2. Hello world, it is now"]
       [clock es]
       [color-input es]])))

