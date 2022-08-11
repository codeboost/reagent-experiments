(ns simpleexample.core-v3
  (:require
    [simpleexample.v3-state :as state]))

(defn greeting [message]
  [:h1 message])

(defn color-input [{:keys [color set-color!]}]
  [:div.color-input
   "Time color: "
   [:input {:type      "text"
            :value     @color
            :on-change set-color!}]])



(= (color-input {:color (reagent.ratom/reaction 33)
                 :set-color! :my-handler})
   [:div.color-input "Time color: " [:input {:type "text", :value 33, :on-change :my-handler}]])


(defn clock [{:keys [formatted-time color]}]
  [:div.example-clock
   {:style {:color @color}}
   @formatted-time])

(defn simple-example []
  (let [sc (state/v3-state)]
    (fn []
      [:div
       [greeting "3. Hello world, it is now"]
       [clock sc]
       [color-input sc]])))