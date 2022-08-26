(ns todomvc.todo-mvc-2-state
  (:require
    [cljs.spec.alpha :as s]
    [reagent.core :as r]
    [todomvc.todo-mvc-2-spec :as v2-spec]
    [reagent.ratom :refer [reaction]]
    [clojure.string :as str]))

(s/fdef todo-input-state
        :args (s/cat :props ::v2-spec/todo-input-props)
        :ret ::v2-spec/todo-input-state)

(defn todo-input-state [{:keys [on-stop on-save]}]
  (let [state (r/atom {})
        set-text! #(swap! state assoc :text %)
        stop! (fn []
                (set-text! "")
                (when on-stop
                  (on-stop)))
        save! (fn []
                (let [v (-> @state :text str str/trim)]
                  (when-not (empty? v)
                    (on-save v))
                  (stop!)))]
    {:set-text! set-text!
     :text      (reaction (:text @state))
     :save!     save!
     :stop!     stop!}))

(defn remove-done [m]
  (into {}
        (filter (fn [[k v]]
                  (not (:done v))) m)))

(defn add-todo!
 ([state text done?]
  (let [id (:id-counter (swap! state update :id-counter inc))]
    (swap! state assoc-in [:todos id] {:id id :title text :done done?})))
 ([state text]
  (add-todo! state text false)))

(defn complete-all [m done?]
  (->>
    m
    (map (fn [[k v]]
           [k (assoc v :done done?)]))
    (into {})))

(defn todo-item-state
  "State interface that deals with a todo item."
  [state todo-k id]
  {:toggle!       #(swap! state update-in [todo-k id :done] not)
   :save!         #(swap! state assoc-in [todo-k id :title] %)
   :delete!       #(swap! state update todo-k (fn [todos] (dissoc todos id)))
   :set-editing!  #(swap! state assoc-in [todo-k id :editing?] true)
   :stop-editing! #(swap! state assoc-in [todo-k id :editing?] false)
   :editing?      (reaction (get-in @state [todo-k id :editing?]))})

(defn todo-app-state [props]
  (let [state (r/atom {:todos          {}
                       :id-counter     0
                       :current-filter :all})
        items (reaction (-> @state :todos vals))
        num-done (reaction (->> @items (filter :done) count))
        current-filter (reaction (:current-filter @state))
        num-active (reaction (- (count @items) @num-done))]
    {:items               items
     :empty-todos?        (reaction (-> @items empty?))
     :num-done            num-done
     :num-active          num-active
     :current-filter      current-filter
     :filtered-items      (reaction
                            (filter
                              (case @current-filter
                                :active (complement :done)
                                :done :done
                                :all identity)
                              @items))
     :set-current-filter! #(swap! state assoc :current-filter %)
     :complete-all!       #(swap! state update :todos (fn [todos]
                                                        (complete-all todos (pos? @num-active))))
     :clear-done!         #(swap! state update :todos remove-done)
     :todo-item-state     #(todo-item-state state :todos %)
     :add-todo!           (fn [& args]
                            (apply add-todo! (concat [state] args)))}))

(defn app-state-with-defaults! [props]
  (let [{:keys [add-todo!] :as app-state} (todo-app-state props)]
    (do
      (add-todo! "Rename Cloact to Reagent" true)
      (add-todo! "Add undo demo" true)
      (add-todo! "Make all rendering async" true)
      (add-todo! "Allow any arguments to component functions" true))
    app-state))

(comment
  (let [{:keys [add-todo! items empty-todos? num-active toggle-todo! set-filter!
                current-filter filtered-items
                clear-done!] :as app-state} (todo-app-state {})]


    (add-todo! "Hello")
    (add-todo! "World")

    (toggle-todo! 2)

    (clear-done!)

    #_@items))

