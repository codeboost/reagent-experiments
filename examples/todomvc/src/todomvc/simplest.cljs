(ns todomvc.simplest
  (:require
    [cljs.spec.alpha :as s]
    [reagent.core :as r]
    [reagent.ratom :as ratom]
    [clojure.string :as str]))


(defn component-1 []
  (let [state (r/atom {:first-name ""
                       :last-name ""})]
    (fn []
      [:div
       [:h3 (str "Hello, " (:first-name @state) " " (:last-name @state))]
       [:hr]
       [:input {:type :text
                :value (:first-name @state)
                :on-change #(swap! state assoc :first-name (-> % .-target .-value))}]
       [:input {:type :text
                :value (:last-name @state)
                :on-change #(swap! state assoc :last-name (-> % .-target .-value))}]])))

(defn component []
  [component-1])
