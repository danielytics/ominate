# ominate

Animation for Om components

### NOTE: This is still alpha - the API has not yet been decided and WILL change! Suggestions welcome.

## Usage
In your project file, add

```clj
[ominate "0.1.0"]
```

## Documentation

Example:

```clj
(ns example.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [ominate.core :refer [ominate]
            [ominate.easing :as ease]
            [ominate.anims :as anims]))

(def animate (async/chan))
(def app-state (atom {:message "Hello, this is a test component!"}))

(defn example-component [props owner opts]
  (om/component
    (dom/div #js {:style #js {:width 500 :height 300}
                  :onClick #(async/put! animate true)}
      (:message props))))

(om/root
  (ominate
    example-component
    {:ch animate          ; Channel used to trigger animation
     :duration 5000       ; Animation will take 5 seconds to complete
     :easing ease/sine-in ; Sine wave based easing function
     :anim anims/fade     ; Fade opacity of component 
     :repeat 2})          ; Repeat animation twice (total duration 10 seconds)
  app-state
  {:target (. js/document (getElementById "example-id"))})
```

All parameters are optional.

The animation can be reversed by composing the easing function with
`ease/reverse`.
For example: `(comp ease/cube-in ease/reverse)`

To play the animation one direction for the first half of the duration and then
reverse the direction for the second half of the duration, compose the easing
function with `ease/forward-back` or `ease/back-forward`.

## Known Bugs

The `ease/elastic-*` and `ease/bounce-*` easing functions are known to not be
working.

## License

Copyright © 2014 Dan Kersten

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
