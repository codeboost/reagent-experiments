(ns todomvc.simplest
  (:require
    [cljs.spec.alpha :as s]
    [reagent.core :as r]
    [reagent.ratom :as ratom :refer [reaction]]
    [clojure.string :as str]))


(defn component-1-state [state]
  (let [first-name (reaction (:first-name @state))
        last-name (reaction (:last-name @state))]
    {:first-name first-name
     :last-name last-name
     :set-first-name! #(swap! state assoc :first-name %)
     :set-last-name! #(swap! state assoc :last-name %)}))

(defn component-1-state-adapter [{:keys [set-first-name! set-last-name!] :as state}]
  (assoc state :handlers {:on-fname-change #(set-first-name! (-> % .-target .-value))
                          :on-lname-change #(set-last-name! (-> % .-target .-value))}))

(defn component-1* [{:keys [first-name last-name]
                     {:keys [on-fname-change on-lname-change]} :handlers}]
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
  (let [state (-> {:first-name "" :last-name ""}
                  r/atom
                  component-1-state
                  component-1-state-adapter)]
    (fn []
      [component-1* state])))


(defn component []
  [component-1])
