(ns ominate.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [ominate.anims :as anims]
            [ominate.easing :as ease]))

(comment
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
          (om/build component props {:opts opts})))))))


(defn ominator [props owner {:keys [easing duration component watch]}]
  (reify
    om/IInitState
    (init-state [_]
      {:ominate-ch  (async/chan)
       :kill-ch     (async/chan)
       :animating?  false})

    om/IDisplayName
    (display-name [_]
      "Ominator")

    om/IWillMount
    (will-mount [_]
      (let [control-ch  (om/get-state owner :ominate-ch)
            kill-ch     (om/get-state owner :kill-ch)]
        (go-loop []
          (let [[control chan] (async/alts! [control-ch kill-ch])]
            (when (= chan control-ch)
              ; Get command and config from control channel
              (let [[command conf]  (cond
                                      (keyword? control) [control {}]
                                      (and (vector? control)
                                           (= 2 (count control))) control
                                      :else [:invalid {}])]
                ; Handle command
                (condp = command
                  ; Animation start command, set up new animation
                  :start (do
                           (om/set-state! owner :animating? true)
                           (om/update! props :ominate-start-time (.now js/Date)))
                  ; Animation stop command, must run notify callback
                  :stop (om/set-state! owner :animating? false)))
              ; Wait for more commands
              (recur))))))

    om/IDidUpdate
    (did-update [_ _ _]
      (when (om/get-state owner :animating?)
        (let [start-time  (:ominate-start-time props)
              now         (.now js/Date)
              elapsed     (- now start-time)
              value       (/ elapsed duration)]
          ; If there is time left, then update state with animation info,
          ; otherwise send a control message to stop the animation
          (if (< elapsed duration)
            (om/transact! props #(assoc % :ominate-value (easing value)
                                          :ominate-time now))
            (async/put! (om/get-state owner :ominate-ch) [:stop {:reason :completed}])))))

    om/IWillUnmount
    (will-unmount [_]
      (async/put (om/get-state owner :kill-ch) true))

    om/IRenderState
    (render-state [_ state]
      ; If a watch function is configured, run it to see if a new animation
      ; should start, but only if animation isn't already running
      (when (and watch
                 (not (:animating? state))
                 (watch props))
        (async/put! (:ominate-ch state) :start))
      ; Build the animated component, passing local state to it, as well as
      ; providing convenience functions through opts
      (om/build component props {:state (dissoc state :kill-ch)
                                 :opts {:ominate-start #(async/put! (:ominate-ch state) :start)
                                        :ominate-stop  #(async/put! (:ominate-ch state) :stop)}}))))

