(ns todomvc.todo-mvc-2-state
  (:require
    [cljs.spec.alpha :as s]
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [reagent.ratom :refer [reaction]]
    [clojure.string :as str]))

(s/def ::on-input-blur fn?)
(s/def ::on-input-changed fn?)
(s/def ::on-input-key-down fn?)
(s/def ::set-text! fn?)
(s/def ::text #(satisfies? IDeref %))
(s/def ::on-save fn?)
(s/def ::on-stop fn?)

(s/def ::todo-input-state (s/keys :req-un [::on-input-blur
                                           ::on-input-changed
                                           ::on-input-key-down
                                           ::set-text!
                                           ::text]))

(s/def ::todo-input-props (s/keys :req-un [::on-save ::on-stop]))

(s/fdef todo-input-state
        :args (s/cat :props ::todo-input-props)
        :ret ::todo-input-state)


(defn todo-input-state [{:keys [on-stop on-save]}]
  (let [state (r/atom {})
        set-text! #(swap! state assoc :text %)
        stop! (fn []
                (set-text! "")
                (when on-stop
                  (on-stop)))
        save! (fn []
                (let [v (-> @state :text str str/trim)]
                  (if-not (empty? v) (on-save v))
                  (stop!)))
        on-key-down (fn [e]
                      (case (.-which e)
                        13 (save!)
                        27 (stop!)
                        nil))
        on-input-changed #(set-text! (-> % .-target .-value))]

    {:set-text!         set-text!
     :text              (reaction (:text @state))
     ;;DOM handlers
     :on-input-blur     save!
     :on-input-changed  on-input-changed
     :on-input-key-down on-key-down}))

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

(defn todo-app-state [props]
  (let [state (r/atom {:todos          {}
                       :id-counter     0
                       :current-filter :all})
        toggle-todo! (fn [id]
                       (swap! state update-in [:todos id :done] not))
        save-todo! (fn [id title]
                     (swap! state assoc-in [:todos id :title] title))
        delete-todo! (fn [id]
                       (swap! state update :todos #(dissoc % id)))
        clear-done! (fn []
                      (swap! state update :todos remove-done))
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

     :complete-all!      (fn []
                           (swap! state update :todos (fn [todos]
                                                        (complete-all todos (pos? @num-active)))))
     :clear-done!         clear-done!
     :delete-todo!        delete-todo!
     :toggle-todo!        toggle-todo!
     :save-todo!          save-todo!
     :set-editing!        (fn [id editing?]
                            (swap! state update-in :todos [id :editing?] editing?))
     :add-todo!           (fn [& args]
                            (apply add-todo! (concat [state] args)))}))

(defn todo-item-state [{:keys [toggle-todo! delete-todo! save-todo!]} {:keys [id] :as item}]
  (let [state (r/atom {:editing? false})]
    {:editing?      (reaction (:editing? @state))
     :set-editing!  #(swap! state assoc :editing? true)
     :stop-editing! #(swap! state assoc :editing? false)
     :save!         #(save-todo! id %)
     :toggle!       #(toggle-todo! id)
     :delete!       #(delete-todo! id)}))

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

