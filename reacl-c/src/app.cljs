(ns app
  (:require [reacl-c.core :as c :include-macros true]
            [reacl-c.dom :as dom :include-macros true]
            [reacl-c.main :as cmain]
            [active.clojure.lens :as lens]
            [active.data.record :refer-macros [def-record]]
            [active.data.realm :as realm]))


; Model
(def-record entry
  [entry-name :- realm/string
   entry-phone-number :- realm/string])

(def empty-entry
  (entry entry-name ""
         entry-phone-number ""))

(def input-item
  (c/dynamic
   (fn [text]
    (dom/input
     {:value text
      :onChange
      (fn [old-text event]
        (c/return
         :state
         (.-value
          (.-target event))))}))))

(def entry-item
  ;; model: entry
  (dom/div
   "Name:"
   (c/focus entry-name input-item)
   "Tel:"
   (c/focus entry-phone-number input-item)))

#_(defn phonebook-item []
  ;; model: sequence-of entry
  (c/dynamic
   (fn [entries]
     (apply
      dom/div
      (map (fn [idx]
             (c/focus
              (lens/at-index idx)
              entry-item))
           (range
            (count entries)))))))

(defn map-item [child-item]
  (c/dynamic
   (fn [xs]
     (apply
      dom/div
      (map (fn [idx]
             (c/focus
              (lens/at-index idx)
              child-item))
           (range
            (count xs)))))))

(def phonebook-item
  (map-item entry-item))

(def phonebook-with-add-button-item
  (dom/div
   phonebook-item
   (dom/button
    {:onClick
     (fn [phonebook _]
       (concat phonebook
               [empty-entry]))}
    "Add new")))

;; ---- The functional view model

(def-record entry-vm
  [entry-vm-entry :- entry
   entry-vm-emphasized? :- realm/boolean])

(def empty-phonebook [])

(defn deemphasize [entry]
  (entry-vm-emphasized? entry false))

(defn add-new-entry [entries]
  (conj (mapv deemphasize entries)
        (entry-vm entry-vm-entry empty-entry
                  entry-vm-emphasized? true)))

;; ---

(defn emphasize [item]
  (dom/div {:style {:border "1px solid blue"}}
           item))

(def entry-item-2
  ;; model: entry-vm
  (c/dynamic
   (fn [entry-vm]
     ((if (entry-vm-emphasized? entry-vm)
        emphasize
        identity)
      (c/focus entry-vm-entry
               entry-item)))))

(def phonebook-item-2
  (map-item entry-item-2))

(def phonebook-with-add-button-item-2
  (dom/div
   phonebook-item-2
   (dom/button
    {:onClick add-new-entry}
    "Add new")))

;; ---

(defn toplevel []
  (c/isolate-state
   []
   (dom/div
    (c/dynamic pr-str)
    phonebook-with-add-button-item-2)))

(defn ^:dev/after-load start []
  (println "start")
  (cmain/run
   (.getElementById js/document "main")
   (toplevel)))

(defn init []
  (println "init")
  (start))

(init)
