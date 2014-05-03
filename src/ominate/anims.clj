(ns ominate.anims
  (:require [domina]))

;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap-fn [f]
  {:on-frame f})

(defn with-overlay [anim-fn style] 
  {:on-frame  (fn [v n s] (anim-fn v s))
   :on-begin  (fn [n]
                ; Create overlay
                (let [style (conj style
                                  {:z-index 1000
                                   :position "absolute"
                                   :width  (str (.-clientWidth n) "px")
                                   :height (str (.-clientHeight n) "px")})]
                  (-> (domina/prepend! n "<div>")
                      domina/children
                      first
                      (domina/set-styles! style))))
   :on-end    (fn [n s]
                ; Delete overlay
                (domina/destroy! s))})

;; Animations
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fade
  "Fade opacity"
  [v n]
  (domina/set-style! n :opacity v))

(defn fade-color
  "Fade color overlay"
  [color]
  (with-overlay
    fade
    {:background-color color :opacity 0}))

