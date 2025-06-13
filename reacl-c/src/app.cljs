(ns app
  (:require [reacl-c.core :as c :include-macros true]
            [reacl-c.dom :as dom :include-macros true]
            [reacl-c.main :as cmain]
            [active.clojure.lens :as lens]
            [active.data.record :refer-macros [def-record]]
            [active.data.realm :as realm]))


(def-record entry
  [entry-name
   entry-phone-number])

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

#_(defn phone-book-item []
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

(def phone-book-item
  (map-item entry-item))

(def phone-book-with-add-button-item
  (dom/div
   phone-book-item
   (dom/button
    {:onClick
     (fn [phone-book _]
       (c/return
        :state
        (concat phone-book
                [empty-entry])))}
    "Add new")))

;; ---- The functional view model

(defn emphasized [item]
  (dom/div {:style {:border "1px solid blue"}}
           item))

(def-record entry-vm
  [entry-entry :- entry
   entry-emphasized? :- realm/boolean])

(defn deemphasize [entry]
  (entry-emphasized? entry false))

(def empty-phone-book [])

(defn add-entry [entries core-entry]
  (conj (mapv deemphasize entries)
        (entry-vm entry-entry core-entry
                  entry-emphasized? true)))

;; ---

(def entry-item-2
  ;; model: entry-vm
  (c/dynamic
   (fn [entry-vm]
     ((if (entry-emphasized? entry-vm)
        emphasized
        identity)
      (dom/div
       (c/focus entry-entry
                entry-item))))))

(def phone-book-item-2
  (map-item entry-item-2))

(def phone-book-with-add-button-item-2
  (dom/div
   phone-book-item-2
   (dom/button
    {:onClick
     (fn [entries _]
       (c/return :state (add-entry entries empty-entry)))}
    "Add new")))

;; ---

(defn toplevel []
  (c/isolate-state
   []
   (dom/div
    (c/dynamic pr-str)
    phone-book-with-add-button-item-2)))

(defn ^:dev/after-load start []
  (println "start")
  (cmain/run
   (.getElementById js/document "main")
   (toplevel)))

(defn init []
  (println "init")
  (start))

(init)
