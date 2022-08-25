(ns todomvc.todo-mvc-2-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [todomvc.todo-mvc-2-state :as state]))


(deftest todo-input-state-test
  (let [{:keys [set-text! text on-input-blur on-input-changed on-input-key-down]}
        (state/todo-input-state {:on-stop #()
                                 :on-save #()})]
    (testing "set-text! should change the text"
      (is (= @text nil))
      (set-text! "My text")
      (is (= @text "My text")))))
