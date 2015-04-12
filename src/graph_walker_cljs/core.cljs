(ns ^:figwheel-always graph-walker-cljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [figwheel.client :as fw]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

;(enable-console-print!)

(defrecord Node [value choice-node-pairs])

(def node-foo {:value "Call Engineering" :choice-node-pairs nil})
(def node-bar {:value "Did you google the problem?" :choice-node-pairs [["Yes" node-foo]]})
(def node-done {:value "File a JIRA ticket and drop a note in Eng chat." :choice-node-pairs nil})
(def node-not-sure {:value "Try to log into the website." :choice-node-pairs nil})
(def node-baz {:value "Does this issue actually impact customers?"
               :choice-node-pairs [["Yes" node-bar]
                                   ["No" node-done]
                                   ["I don't know" node-not-sure]]})

(defonce app-state
  (atom
   {:nodes-root node-baz
    :current-node node-baz}))

(defn node-view [{:keys [value choice-node-pairs]} owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [node-move]}]
      (dom/div nil
               (dom/div nil value)
               (apply dom/div nil
                (for [[button-label node-to-goto] choice-node-pairs]
                  (dom/button #js {:onClick (fn [e] (put! node-move node-to-goto))}
                              button-label)))))))

(defn node-walk [xs] xs)

(defn contacts-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:node-move (chan)})
    om/IWillMount
    (will-mount [_]
      (let [node-move (om/get-state owner :node-move)]
        (go (loop []
              (let [next-node (<! node-move)]
                (om/transact! data :current-node (fn [_] next-node))
                (recur))))))
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/h2 nil "Graph State")
        (dom/div nil
          (om/build node-view (:current-node data)
            {:init-state state}))))))

(om/root contacts-view app-state
  {:target (. js/document (getElementById "graph"))})
