(ns ^:figwheel-always graph-walker-cljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [figwheel.client :as fw]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [clojure.data :as data]
            [clojure.string :as string]))

;(enable-console-print!)

(def graph (js->clj js/graph))

(defn node-name-to-contents [node-name]
  (get-in graph ["nodes" node-name]))

(defonce app-state
  (atom
   {:nodes-root (node-name-to-contents (get graph "starting-node"))
    :current-node (node-name-to-contents (get graph "starting-node"))}))

(defn node-view [node-content owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [node-move]}]
    (let [value (get node-content "value") 
          choice-node-pairs (get node-content "choice-node-pairs")]
      (dom/div nil
               (dom/div nil value)
               (apply dom/div nil
                (for [[button-label node-to-goto] choice-node-pairs]
                  (dom/button #js {:onClick (fn [e] (put! node-move node-to-goto))}
                              button-label))))))))

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
                (om/transact! data :current-node (fn [_] (node-name-to-contents next-node)))
                (recur))))))
    om/IRenderState
    (render-state [this state]
      (dom/div nil
        (dom/h2 nil "Graph State")
        (dom/div nil
          (om/build node-view (:current-node data)
            {:init-state state}))
      (dom/button #js {:onClick (fn [e] (om/transact! data (fn [{:keys [nodes-root] :as the-data}] 
                                                             (assoc the-data :current-node nodes-root))))} 
                  "Reset")))))

(om/root contacts-view app-state
  {:target (. js/document (getElementById "graph"))})
