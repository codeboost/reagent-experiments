(ns todomvc.simplest
  (:require
    [cljs.spec.alpha :as s]
    [reagent.core :as r]
    [reagent.ratom :as ratom :refer [reaction]]
    [clojure.string :as str]))

(defn component-1* [{:keys [first-name last-name on-fname-change on-lname-change]}]
  [:div
   [:h3 (str "Hello, " @first-name " " @last-name)]
   [:hr]
   [:input {:type :text
            :value @first-name
            :on-change on-fname-change}]
   [:input {:type :text
            :value @last-name
            :on-change on-lname-change}]])

(defn component-1 []
  (let [state (r/atom {:first-name ""
                       :last-name ""})
        first-name (reaction (:first-name @state))
        last-name (reaction (:last-name @state))
        set-first-name! #(swap! state assoc :first-name %)
        set-last-name! #(swap! state assoc :last-name %)
        on-fname-change #(set-first-name! (-> % .-target .-value))
        on-lname-change #(set-last-name! (-> % .-target .-value))]
    (fn []
      [component-1* {:first-name first-name
                     :last-name last-name
                     :on-fname-change on-fname-change
                     :on-lname-change on-lname-change}])))


(defn component []
  [component-1])
