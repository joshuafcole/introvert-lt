(ns lt.plugins.introvert
  (:require [clojure.string :as string]
            [lt.object :as object]
            [lt.objs.command :as cmd]
            [lt.objs.tabs :as tabs]
            [lt.util.dom :as dom]
            [overcode.introvert :as introvert]
            [crate.core :as crate]
            [crate.binding :refer [bound]])
  (:require-macros [lt.macros :refer [defui behavior]]))

(def util (js/require "util"))

(defn get-var
  ([v] (get-var v js/window))
  ([v context]
   (let [split (string/split v #"\.")
         ctx (aget context (first split))
         remainder (rest split)]
     (if (> (count remainder) 0)
       (get-var (string/join "." remainder) ctx)
       ctx))))

(defn render-obj [obj]
  (let [out (introvert/->js obj true)]
    (js/console.log out)
    (.inspect util out)))


(defui inspect-btn [this]
  [:button "inspect"]
  :click (fn []
           (let [value (.-value (dom/$ :.add-input (object/->content this)))]
             (let [obj (get-var value)
                   $introverts (dom/$ :.introverts (object/->content this))
                   li (crate/html [:li {:id (str "introvert-" value)}
                                   [:pre (render-obj obj)]])]
               ;; create an li element #introvert-<value>
               ;; containing the JSON representation of the object
               ;; With circular references collapsed.
               ;; Inserted into :ul.introverts

               (dom/append $introverts li)
               ))))

(object/object* ::introvert
                :tags [:introvert]
                :name "introvert"
                :init (fn [this]
                        [:div {:id "introvert"}
                         [:h1 "Introvert"]
                         [:input.add-input {:type "text" :placeholder "Object to analyze"}]
                         (inspect-btn this)
                         [:ul.introverts]]))

(def introvert (object/create ::introvert))

(behavior ::on-close-destroy
          :triggers #{:close}
          :reaction (fn [this]
                      (when-let [ts (:lt.objs.tabs/tabset @this)]
                        (when (= (count (:objs @ts)) 1)
                          (tabs/rem-tabset ts)))
                      (object/raise this :destroy)))

(cmd/command {:command :introvert.show
              :desc "Introvert: Show Introvert"
              :exec (fn []
                      (tabs/add-or-focus! introvert))})
