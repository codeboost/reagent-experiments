(ns todomvc.todo-mvc-2
  (:require
    [reagent.dom :as rdom]
    [todomvc.todo-mvc-2-state :as v2s]))

;;F1 component, Unit testable
(defn todo-input- [{:keys [id class placeholder]} {:keys [text on-input-blur on-input-changed on-input-key-down]}]
  [:input {:type        "text" :value @text
           :id          id :class class :placeholder placeholder
           :on-blur     on-input-blur
           :on-change   on-input-changed
           :on-key-down on-input-key-down}])

;;F2 component
(defn todo-input [props]
  (let [state (v2s/todo-input-state props)]
    (fn [props]
      [todo-input- props state])))

(def todo-edit (with-meta todo-input
                          {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn todo-item [app-state item]
  (let [{:keys [editing? set-editing! toggle! delete! save! stop-editing!]} (v2s/todo-item-state app-state item)]
    (fn [_ {:keys [id done title]}]
      [:li {:class (str (if done "completed ")
                        (if @editing? "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox"
                        :checked done
                        :on-change toggle!}]
        [:label {:on-double-click set-editing!} title]
        [:button.destroy {:on-click delete!}]]
       (when @editing?
         [todo-edit {:class   "edit" :title title
                     :on-save save!
                     :on-stop stop-editing!}])])))

(defn todo-stats [{:keys [current-filter set-current-filter! num-active num-done
                          clear-done!]}]
  (let [props-for (fn [name]
                    {:class    (if (= name @current-filter) "selected")
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
  (let [{:keys [add-todo! empty-todos? num-active filtered-items complete-all!] :as app-state}
        (v2s/app-state-with-defaults! {})]
    (fn []
      [:div
       [:section#todoapp
        [:header#header
         [:h1 "todos"]
         [todo-input-f2 {:id          "new-todo"
                         :placeholder "What needs to be done?"
                         :on-save     add-todo!}]

         (when-not @empty-todos?
           [:div
            [:section#main
             [:input#toggle-all {:type      "checkbox" :checked (zero? @num-active)
                                 :on-change complete-all!}]
             [:label {:for "toggle-all"} "Mark all as complete"]
             [:ul#todo-list
              (for [todo @filtered-items]
                ^{:key (:id todo)} [todo-item app-state todo])]]
            [:footer#footer
             [todo-stats app-state]]])]]

       [:footer#info
        [:p "Double-click to edit a todo"]]])))
