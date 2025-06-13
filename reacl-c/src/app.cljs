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

(def entry-item-2
  ;; model: entry
  (dom/div
   entry-item
   (dom/button
    {:onClick
     (fn [_ _]
       (c/return :action :delete))}
    "Delete")))

(defn phone-book-item []
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

(defn map-item-2
  [child-item reduce-action-f]
  (c/dynamic
   (fn [xs]
     (apply
      dom/div
      (map (fn [idx]
             (c/handle-action
              (c/focus
               (lens/at-index idx)
               child-item)
              (fn [_ ac]
                (c/return
                 :action
                 (reduce-action-f idx ac)))))
           (range
            (count xs)))))))

(def phone-book-item-2
  (map-item entry-item))

(def phone-book-item-3
  (map-item-2 entry-item-2
              (fn [idx _action]
                [:delete idx])))

(defn phone-book-with-add-button-item []
  (dom/div
   phone-book-item-2
   (dom/button
    {:onClick
     (fn [phone-book _]
       (c/return
        :state
        (concat phone-book
                [empty-entry])))}
    "Add new")))

(defn remove-entry-at-index
  [coll idx]
  (into (subvec coll 0 idx)
        (subvec coll (inc idx))))

(defn handle-phone-book-actions [phone-book action]
  (assert (vector? action))
  (case (first action)
    :add
    (conj phone-book
          empty-entry)

    :delete
    (remove-entry-at-index
     phone-book
     (second action))))

(def phone-book-with-add-button-item-2
  (c/handle-action
   (dom/div
    phone-book-item-3
    (dom/button
     {:onClick
      (fn [phone-book _]
        (c/return :action [:add]))}
     "Add new"))
   (fn [phone-book action]
     (c/return
      :state
      (handle-phone-book-actions phone-book action)))))

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

(defn delete-entry-at-index [entries idx]
  (remove-entry-at-index entries idx))

;; ---

(def entry-item-3
  ;; model: entry-vm
  (c/dynamic
   (fn [entry-vm]
     ((if (entry-emphasized? entry-vm)
        emphasized
        identity)
      (dom/div
       (c/focus entry-entry
                entry-item-2))))))

(defn handle-phone-book-actions-2 [phone-book action]
  (assert (vector? action))
  (case (first action)
    :add
    (add-entry phone-book empty-entry)

    :delete
    (delete-entry-at-index phone-book (second action))))

(def phone-book-item-4
  (map-item-2 entry-item-3
              (fn [idx _action]
                [:delete idx])))

(def phone-book-item-5
  (c/handle-action
   (dom/div
    phone-book-item-4
    (dom/button
     {:onClick
      (fn [phone-book _]
        (c/return :action [:add]))}
     "Add new"))
   (fn [phone-book action]
     (c/return
      :state
      (handle-phone-book-actions-2 phone-book action)))))

(defn toplevel []
  (c/isolate-state
   []
   (dom/div
    (c/dynamic pr-str)
    phone-book-item-5)))

(defn ^:dev/after-load start []
  (println "start")
  (cmain/run
   (.getElementById js/document "main")
   (toplevel)))

(defn init []
  (println "init")
  (start))

(init)
