(ns ominate.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [cljs.core.async :as async]))

(defn ominator [props owner opts]
  (reify
    om/IInitState
    (init-state [_]
      (let [{:keys [easing duration component notify]} opts]
        {:ominate-ch  (async/chan)
         :kill-ch     (async/chan)   
         :animating?  false  
         :notify      notify
         :duration    (or duration 1000)   ; 1 second default duration
         :easing-fn   (or easing identity) ; Linear default easing
         :component   component}))

    om/IDisplayName
    (display-name [_]
      (str "Ominator-" (:name opts)))

    om/IWillMount
    (will-mount [_]
      (let [control-ch  (om/get-state owner :ominate-ch)
            kill-ch     (om/get-state owner :kill-ch)]
        ; Wait for control messages
        (go-loop []
          (let [[control chan] (async/alts! [control-ch kill-ch])]
            (when (= chan control-ch)
              ; Get command and config from control channel
              (let [[command conf]
                      (cond
                        (keyword? control)          [control {}]
                        (and (vector? control)
                             (= 2 (count control))) control
                        :else                       [:invalid {}])]
                ; Handle command
                (condp = command
                  ; Animation start command, set up new animation
                  :start
                    (let [{:keys [duration easing notify component]} conf]
                      ; Set the optional configuration, if provided
                      (when duration (om/set-state! owner :duration duration))
                      (when easing (om/set-state! owner :easing easing))
                      (when component (om/set-state! owner :component component))
                      (when notify (om/set-state! owner :notify notify))
                      ; Set the start time and start animating by setting
                      ; the animating? flag to true
                      (om/update! props :ominate-start-time (.now js/Date))
                      (om/set-state! owner :animating? true))
                  ; Animation stop command, must run notify callback if
                  ; configured
                  :stop
                    (let [completion-callback (om/get-state owner :notify)
                          reason (or (:reason conf) :aborted)]
                      ; Stop animation by resetting the animating? flag
                      (om/set-state! owner :animating? false)
                      ; If a completion notification callback was set, then call
                      ; it with the animation name and stop reason now
                      (when completion-callback
                        (completion-callback (:name opts) reason)))))
              ; Wait for more commands
              (recur))))))

    om/IDidUpdate
    (did-update [_ _ _]
      (when (om/get-state owner :animating?)
        (let [easing-fn   (om/get-state owner :easing-fn)
              start-time  (:ominate-start-time props)
              now         (.now js/Date)
              elapsed     (- now start-time)
              value       (/ elapsed duration)
              value       (if (> value 1) 1 value)]
          ; If there is time left, then update state with animation info,
          ; otherwise send a control message to stop the animation
          (if (< elapsed duration)
            (om/transact! props #(assoc % :ominate-value (easing-fn value)
                                          :ominate-elapsed elapsed
                                          :ominate-time now))
            (async/put! (om/get-state owner :ominate-ch)
                        [:stop {:reason :completed}])))))

    om/IWillUnmount
    (will-unmount [_]
      (async/put (om/get-state owner :kill-ch) true))

    om/IRenderState
    (render-state [_ state]
      (let [component  (:component state)
            watch-fn   (:watch opts)
            control-ch (:ominate-ch state)]
        ; If a watch function is configured, run it to see if a new animation
        ; should start, but only if animation isn't already running
        (when (and watch-fn
                   (not (:animating? state))
                   (watch-fn props))
          (async/put! contorl-ch :start))
        ; Build the animated component, passing local state to it, as well as
        ; using opts to provide convenience functions to start and stop the
        ; animations
        (om/build component props
          {:state (dissoc state :kill-ch :easing-fn :component)
           :opts {:ominate-start #(async/put! control-ch :start)
                  :ominate-stop  #(async/put! control-ch :stop)}})))))

