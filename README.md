# ominate

Animation for Om components

### NOTE: This is still alpha - the API has not yet been decided and WILL change! Suggestions welcome.

## Usage
In your project.clj file, add

```clj
[ominate "0.1.0"]
```

Then import the namespaces:

`(require '[ominate.core :refer [ominate]])` - the API entry point.

`(require '[ominate.easing :as ease])` - easing functions (optional, defaults to
linear).

`(require '[ominate.anims :as anims])` - pre-built animations (optional,
defaults to fading opacity).

## Example

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
    (dom/div #js {:style #js {:width 500 :height 300 :background-color "#00f"}
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

## Documentation

#### ominate

```clj
(defn ominate [component config]
  ...)
```

Animate Om component `component`, applying options from map `config`.
Wrap this function around the component you wish to animate before passing to
`om/build`, `om/build-all` or `om/root`.

All `config` options options have reasonable defaults and can be omitted. The
following options are available:

`:ch` - a core.async channel used to trigger animations.

`:duration` - the duration of the animation in milliseconds. Defaults to 1000.

`:repeat` - the number of times to repeat the animation. Defaults to 0. Note
that the total length of time that the animation plays for will be `:repeat`
times `:duration`.

`:easing` - the easing funciton to apply. Defaults to `ease/linear`.

`:anim` - the animation to play. Defaults to `anims/fade`.

`:watch` - a predicate function to apply to the app-state cursor: `(fn [props]
...)`. When the cursor's state is modified and this function returns true, the
animation will be triggered.

`:notify` - a function `(fn [name] ...)` that is called when the animation has
completed.

`:name` - allows the animation to be named, so that notify functions can be used
for multiple animations and tell which animation completed. Also appended to the
`om/IDisplayName` string.

#### Easing

The default easing function is `ease/linear`.

Available easing funcitons are `liner`, `quad-*`, `cube-*`, `quart-*`,
`quint-*`, `sine-*`, `circular-*`, `exp-*`, `elastic-*`, `back-*` and
`bounce-*` where `*` can be one of `in`, `out` or `in-out`.

Easing functions are functions take take in a value between 0 and 1 and return a
new modified value that is passed to the animations, eg `(defn quad-in [v] (* v
v))`.

The animation can be reversed by composing the easing function with
`ease/reverse`.
For example: `(comp ease/cube-in ease/reverse)`

To play the animation one direction for the first half of the duration and then
reverse the direction for the second half of the duration, compose the easing
function with `ease/forward-back` or `ease/back-forward`.

#### Animations

Animations can be functions `(fn [value dom-node] ...)` or maps:

```clj
{:on-frame (fn [value dom-node state] ...)
 :on-begin (fn [dom-node]
...)
 :on-end (fn [dom-node state] ...)}
```

Available animations are:

`anim/fade` - fade the opacity of the component.

`(anim/fade-color color)` - create a color overlay and fade its opacity

To apply any animation to an overlay, the `(fn with-overlay anim-fn style)` is
provided. For example, `anim/fade-color` is defined as:

```clj
(defn fade-color [color] (with-overlay anim/fade {:background-color color :opacity 0}))
```

## Known Bugs

The `ease/elastic-*` and `ease/bounce-*` easing functions are known to not be
working.

## License

Copyright © 2014 Dan Kersten

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
