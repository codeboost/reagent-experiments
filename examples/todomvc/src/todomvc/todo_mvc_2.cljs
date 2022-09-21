(ns todomvc.todo-mvc-2
  (:require
    [cljs.spec.alpha :as s]
    [reagent.dom :as rdom]
    [todomvc.todo-mvc-2-state :as v2s]
    [todomvc.todo-mvc-2-spec :as spec]))


(s/def ::placeholder string?)
(s/def ::class (s/or :keyword keyword? :string string?))
(s/def ::on-input-blur fn?)
(s/def ::on-input-changed fn?)
(s/def ::on-input-key-down fn?)

(s/def ::todo-input-state (s/keys
                            :req-un [::on-input-blur
                                     ::on-input-changed
                                     ::on-input-key-down
                                     ::spec/text
                                     ::spec/set-text!]))

(s/def ::todo-input-props (s/keys :req-un []
                                  :opt-un [::id ::placeholder ::class]))

(s/fdef todo-input*
  :args (s/cat :props ::todo-input-props
               :state ::spec/todo-input-state)
  :ret vector?)

;;F1 component, Unit testable
(defn todo-input* [{:keys [id class placeholder]} {:keys [text on-input-blur on-input-changed on-input-key-down] :as state}]
  [:input {:type        "text"
           :value       @text
           :id          id
           :class       class
           :placeholder placeholder
           :on-blur     on-input-blur
           :on-change   on-input-changed
           :on-key-down on-input-key-down}])

;;Exposes DOM event handlers which extract the event values and call
;;functions on the state interface.
(defn input-state-adapter [{:keys [set-text! save! stop!] :as state}]
  (assoc state
    :on-input-changed #(set-text! (-> % .-target .-value))
    :on-input-blur save!
    :on-input-key-down (fn [e]
                         (case (.-which e)
                           13 (save!)
                           27 (stop!)
                           nil))))

;;F2 component
(defn todo-input [props]
  (let [state (v2s/todo-input-state props)]
    (fn [props]
      [todo-input* props (input-state-adapter state)])))

(def todo-edit (with-meta todo-input
                          {:component-did-mount #(.focus (rdom/dom-node %))}))

(defn todo-item-adapter
  "Abstracts away the concrete state."
  [{:keys [editing? set-editing! toggle! delete! save! stop-editing!] :as state}]
  {:input-on-change toggle!
   :label-on-double-click set-editing!
   :button-on-click delete!
   :edit-on-save save!
   :edit-on-stop stop-editing!
   :editing? editing?})

(defn todo-item* [{:keys [id done title]} {:keys [editing? input-on-change label-on-double-click button-on-click
                                                  edit-on-save edit-on-stop]}]
  [:li {:class (str (if done "completed ")
                    (if @editing? "editing"))}
   [:div.view
    [:input.toggle {:type      "checkbox"
                    :checked   done
                    :on-change input-on-change}]
    [:label {:on-double-click label-on-double-click} title]
    [:button.destroy {:on-click button-on-click}]]
   (when @editing?
     [todo-edit {:class   "edit"
                 :title title
                 :on-save edit-on-save
                 :on-stop edit-on-stop}])])

(defn todo-item [item {:keys [todo-item-state]}]
  (let [state (todo-item-state (:id item))]
    [todo-item* item (todo-item-adapter state)]))

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
         [todo-input {:id          "new-todo"
                      :placeholder "What needs to be done?"
                      :on-save     add-todo!
                      :on-stop     #()}]

         (when-not @empty-todos?
           [:div
            [:section#main
             [:input#toggle-all {:type      "checkbox" :checked (zero? @num-active)
                                 :on-change complete-all!}]
             [:label {:for "toggle-all"} "Mark all as complete"]
             [:ul#todo-list
              (for [todo @filtered-items]
                ^{:key (:id todo)} [todo-item todo app-state])]]
            [:footer#footer
             [todo-stats app-state]]])]]

       [:footer#info
        [:p "Double-click to edit a todo"]]])))
