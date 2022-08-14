(ns todomvc.todo-mvc-2
  (:require [reagent.core :as r]
            [reagent.dom :as rdom]
            [reagent.ratom :refer [reaction]]
            [clojure.string :as str]))

(defn todo-input-state [{:keys [title on-stop on-save]}]
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

(defn todo-input [{:keys [title on-save on-stop] :as props}]
  (let [{:keys [text on-input-blur on-input-changed on-input-key-down]} (todo-input-state props)]
    (fn [{:keys [id class placeholder]}]
      [:input {:type        "text" :value @text
               :id          id :class class :placeholder placeholder
               :on-blur     on-input-blur
               :on-change   on-input-changed
               :on-key-down on-input-key-down}])))

(def todo-edit (with-meta todo-input
                          {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn remove-done [m]
  (into {}
    (filter (fn [[k v]]
              (not (:done v))) m)))

(defn todo-app-state [props]
  (let [state (r/atom {:todos      {}
                       :id-counter 0
                       :current-filter :all})
        toggle-todo! (fn [id]
                       (swap! state update-in [:todos id :done] not))
        save-todo! (fn [id title]
                     (swap! state assoc-in [:todos id :title] title))
        delete-todo! (fn [id]
                       (swap! state update :todos #(dissoc % id)))

        clear-done!  (fn []
                       (swap! state update :todos remove-done))
        items (reaction (-> @state :todos vals))
        num-done (reaction (->> @items (filter :done) count))
        current-filter (reaction (:current-filter @state))]
    {:items        items
     :empty-todos? (reaction (-> @items empty?))
     :num-done     num-done
     :num-active   (reaction (- (count @items) @num-done))

     :current-filter current-filter
     :filtered-items (reaction
                       (filter
                         (case @current-filter
                           :active (complement :done)
                           :done :done
                           :all identity)
                         @items))

     :set-current-filter! #(swap! state assoc :current-filter %)

     :clear-done!  clear-done!
     :delete-todo! delete-todo!
     :toggle-todo! toggle-todo!
     :save-todo!   save-todo!
     :set-editing! (fn [id editing?]
                     (swap! state update-in :todos [id :editing?] editing?))
     :add-todo!    (fn [text]
                     (let [id (:id-counter (swap! state update :id-counter inc))]
                       (swap! state assoc-in [:todos id] {:id id :title text :done false})))}))

(comment
  (let [{:keys [add-todo! items empty-todos? num-active toggle-todo! set-filter!
                current-filter filtered-items
                clear-done!] :as app-state} (todo-app-state {})]


    (add-todo! "Hello")
    (add-todo! "World")

    (toggle-todo! 2)

    (clear-done!)



    #_@items))


(defn todo-item-state [{:keys [toggle-todo! delete-todo! save-todo!]} id]
  (let [state (r/atom {:editing? false})]
    {:editing?     (reaction (:editing? @state))
     :set-editing! #(swap! state assoc :editing? true)
     :stop-editing! #(swap! state assoc :editing? false)
     :save!        #(save-todo! id %)
     :toggle!      #(toggle-todo! id)
     :delete!      #(delete-todo! id)}))

(comment
  (let [{:keys [add-todo! todos] :as app-state} (todo-app-state {})
        {:keys [toggle!]} (todo-item-state app-state 1)]
    @todos))

(defn todo-item [app-state {:keys [id done title]}]
  (let [{:keys [editing? set-editing! toggle! delete! save! stop-editing!]} (todo-item-state app-state id)]
    (fn [_ {:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing? "editing"))}
       [:div.view
        [:input.toggle {:type      "checkbox" :checked done
                        :on-change #(toggle! id)}]
        [:label {:on-double-click set-editing!} title]
        [:button.destroy {:on-click delete!}]]
       (when @editing?
         [todo-edit {:class   "edit" :title title
                     :on-save save!
                     :on-stop stop-editing!}])])))



(defn todo-stats [{:keys [current-filter set-current-filter! num-active num-done
                          clear-done!]}]
  (let [props-for (fn [name]
                    {:class (if (= name @current-filter) "selected")
                     :on-click #(set-current-filter! name)})]
    [:div
     [:span#todo-count
      [:strong @num-active] " " (case @num-active 1 "item" "items") " left"]
     [:ul#filters
      [:li [:a (props-for :all) "All"]]
      [:li [:a (props-for :active) "Active"]]
      [:li [:a (props-for :done) "Completed"]]]
     (when (pos? @num-done)
       [:button#clear-completed {:on-click clear-done!}
        "Clear completed " @num-done])]))

(defn todo-app []
  (let [{:keys [add-todo! items empty-todos? num-active
                current-filter filtered-items] :as app-state} (todo-app-state {})]
    (fn []
      [:div
       [:section#todoapp
        [:header#header
         [:h1 "todos"]
         [todo-input {:id          "new-todo"
                      :placeholder "What needs to be done?"
                      :on-save     add-todo!}]

         (when-not @empty-todos?
           [:div
            [:section#main
             [:input#toggle-all {:type "checkbox" :checked (zero? @num-active)
                                 :on-change #()}]
             [:label {:for "toggle-all"} "Mark all as complete"]
             [:ul#todo-list
              (for [todo @filtered-items]
                ^{:key (:id todo)} [todo-item app-state todo])]]
            [:footer#footer
             [todo-stats app-state]]])]]

       [:footer#info
        [:p "Double-click to edit a todo"]]])))



