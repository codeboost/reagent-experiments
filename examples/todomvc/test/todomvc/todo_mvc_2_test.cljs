(ns todomvc.todo-mvc-2-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [todomvc.todo-mvc-2 :as mvc2]
    [todomvc.todo-mvc-2-state :as v2s]))

(defn counted-fn
  "Returns a map with keys `title`-calls and `title`-fn.
  `title`-fn is a function, which, when called, saves and stores the arguments in an atom.
  `title`-calls is the atom where the returned function saves the arguments when it is called.
  The returned fn can be used as an instrumented callback function, to test if and how the callback has been called.
  `ret` is the return value that the callback returns."
  [title ret]
  (let [calls (atom [])]
    (into {}
          [[(keyword (str (name title) "-calls")) calls]
           [(keyword (str (name title) "-fn")) (fn [& vs]
                                                 (swap! calls conj vs)
                                                 ret)]])))

(let [o (clj->js {:target {:value 43}})]
  (-> o .-target .-value))

(deftest todo-input-state-test
  (let [{:keys [on-save-calls on-save-fn]} (counted-fn :on-save nil)
        {:keys [on-stop-calls on-stop-fn]}  (counted-fn :on-stop nil)
        {:keys [set-text! text save!]}
        (v2s/todo-input-state {:on-stop on-stop-fn
                               :on-save on-save-fn})]
    (testing "set-text! should change the text"
      (is (= @text nil))
      (set-text! "My text   ")
      (is (= @text "My text   ")))

    (testing "save! clear text and call on-save with trimmed text, then calls `on-stop`"
      (save!)
      (is (= @text ""))
      (is (= @on-save-calls [["My text"]]))
      (is (= @on-stop-calls [nil])))))

;;This demonstrates how to test the 'state adapter' - eg. the state expanded with the dom event handlers.
(deftest todo-input-state-adapter-test
  (let [{:keys [on-save-calls on-save-fn]} (counted-fn :on-save nil)
        {:keys [text] :as tis} (v2s/todo-input-state {:on-save on-save-fn})
        {:keys [on-input-changed on-input-blur on-input-key-down]} (mvc2/input-state-adapter tis)]

    (testing "Text should be updated on input-changed"
      (on-input-changed (clj->js {:target {:value "My text  "}}))
      (is (= @text "My text  ")))

    (testing "Should call on-save on input blur and clear the text"
      (on-input-blur)
      (is (= [["My text"]] @on-save-calls))
      (is (= "" @text)))

    (testing "On escape it should clear the text"
      (on-input-changed (clj->js {:target {:value "Something"}}))
      (is (= @text "Something"))
      (on-input-key-down (clj->js {:which 27}))
      (is (= @text "")))))

(deftest todo-input-component-test
  (let [{:keys [on-save-calls on-save-fn]} (counted-fn :on-save nil)
        {:keys [on-stop-calls on-stop-fn]}  (counted-fn :on-stop nil)
        {:keys [text] :as tis} (v2s/todo-input-state {:on-save on-save-fn
                                                      :on-stop on-stop-fn})
        {:keys [on-input-changed on-input-blur on-input-key-down] :as state} (mvc2/input-state-adapter tis)
        component (mvc2/todo-input {:on-save on-save-fn
                                    :on-stop on-stop-fn})]

    (mvc2/todo-input* {:id "id"
                       :class "class"
                       :placeholder "nothing"} state)
    #_(reagent.dom/render [component {:id "id"
                                      :class "class"
                                      :placeholder "nothing"}] (. js/document (createElement "div")))))


(deftest todo-item-test
  (let [{:keys [items todo-item-state] :as state} (v2s/app-state-with-defaults! {})
        item-state (todo-item-state 1)]
    (mvc2/todo-item* (first @items) item-state)))






