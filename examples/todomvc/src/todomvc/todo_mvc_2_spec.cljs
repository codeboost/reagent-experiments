(ns todomvc.todo-mvc-2-spec
  (:require
    [cljs.spec.alpha :as s]))

(s/def ::set-text! fn?)
(s/def ::save! fn?)
(s/def ::stop! fn?)
(s/def ::text #(satisfies? IDeref %))
(s/def ::on-save fn?)
(s/def ::on-stop fn?)

(s/def ::todo-input-state (s/keys :req-un [::set-text! ::text ::save! ::stop!]))
(s/def ::todo-input-props (s/keys :req-un [::on-save ::on-stop]))