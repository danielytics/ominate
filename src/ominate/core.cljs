(ns ominate.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [ominate.anims :as anims]
            [ominate.easing :as ease]))

(defn trigger-animation [{:keys [owner name anim easing duration repeat notify state] :as conf}]
  (let [start-time (.now js/Date)
        frame      (:on-frame anim)
        begin      (:on-begin anim)
        end        (:on-end anim)
        state      (if (and (nil? state) begin)
                     (begin (om/get-node owner))
                     state)]
    (js/setTimeout
      (fn do-frame []
        (let [elapsed     (- (.now js/Date) start-time)
              ; Get interpolated value
              v           (/ elapsed duration)
              ; Clamp maximum value to 1.0
              v           (if (> v 1.0) 1.0 v)
              node        (om/get-node owner)]
          (when node
            ; Apply animation
            (frame (easing v) node state))
          ; If there is time left, then schedule again
          (if (and node (< elapsed duration))
            (js/setTimeout do-frame 25)
            (if (and node (> repeat 0))
              (trigger-animation (assoc conf :repeat (dec repeat) :state state))
              (do
                (when end (end node state))
                (when notify (notify name)))))))
      25)))

(defn ominate [component & [{:keys [easing duration anim repeat ch name] :as conf}]]
  (let [easing-fn  (if easing easing ease/linear) ; Default to linear easing
        duration-t (if duration duration 1000)    ; Default to 1 second duration
        anim-conf  (if anim anim anims/fade)      ; Default animation to fade in
        anim-conf  (if (not (map? anim-conf))    ; If anim is a not a map, wrap it
                     (anims/wrap-fn anim-conf)
                     anim-conf)
        rep-count  (if repeat repeat 0)
        fixed-conf (conj conf {:easing easing-fn
                               :duration duration-t
                               :anim anim-conf 
                               :repeat rep-count})]
    (fn [props owner opts]
      (reify
        om/IInitState
        (init-state [_]
          {:anim-ch (if ch ch (async/chan))
           :kill-ch (async/chan)})
        om/IDisplayName
        (display-name [_]
          (if name
            (str "Ominate[" name "]")
            "Ominate"))
        om/IDidMount
        (did-mount [_]
          (let [anim-ch (om/get-state owner :anim-ch)
                kill-ch (om/get-state owner :kill-ch)]
            (go-loop []
              (let [[v c] (async/alts! [anim-ch kill-ch])]
                (when (= c anim-ch)
                  (trigger-animation (conj fixed-conf
                                           {:owner owner}
                                           (if (map? v) v {})))
                  (recur))))))
        om/IWillUnmount
        (will-unmount [_]
          (async/put! (om/get-state owner :kill-ch) true))
        om/IRender
        (render [_]
          (when (and watch (watch props))
            (async/put! (om/get-state owner :anim-ch) true))
          (om/build component props {:opts opts}))))))

