(ns ominate.easing
  (:refer-clojure :exclude [reverse]))

;; Modifiers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; To use these with an easing function, compose them: (comp linear forward-back)

(defn reverse
  "Reverse the animation"
  [x]
  (- 1 x))

(defn forward-back
  "Play animation for half duraion, then play reverse animation for half duration"
  [x]
  (if (< x 0.5)
    (* 2 x)
    (- 1 (* 2 (- x 0.5)))))

(defn back-forward
  "Play reverse animation for half duraion, then play animation for half duration "
  [x]
  (if (< v 0.5)
    (- 1 (* 2 v))
    (- (* 2 v) 1)))

;; Easing functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Adapted from https://github.com/warrenm/AHEasing

(def PI   (.-PI js/Math))
(def PI/2 (/ (.-PI js/Math) 2))

(def linear identity)

(defn quad-in
  "Modeled after the parabola y = x^2"
  [p]
  (* p p))

(defn quad-out
  "Modeled after the parabola y = -x^2 + 2x"
  [p]
  (- (* p (- p 2))))

(defn quad-in-out
  "Modeled after the piecewise quadratic
   y = (1/2)((2x)^2)               [0, 0.5)
   y = -(1/2)((2x-1)*(2x-3) - 1)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* 2 p p)
    (+ (* 4 p) (* -2 p p) -1)))

(defn cude-in
  "Modeled after the cubic y = x^3"
  [p]
  (* p p p))

(defn cube-out
  "Modeled after the cubic y = (x - 1)^3 + 1"
  [p]
  (let [f (dec p)]
    (* f f f 1)))

(defn cube-in-out
  "Modeled after the piecewise cubic
   y = (1/2)((2x)^3)         [0, 0.5)
   y = (1/2)((2x-2)^3 + 2)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* p p p 4)
    (let [f (- (* 2 p) 2)]
      (* 0.5 f f f 1))))

(defn quart-in
  "Modeled after the quartic x^4"
  [p]
  (* p p p p))

(defn quart-out
  "Modeled after the quartic y = 1 - (x - 1)^4"
  [p]
  (let [f (dec p)]
    (inc (* f f f (- 1 p)))))

(defn quart-in-out
  "Modeled after the piecewise quartic 
   y = (1/2)((2x)^4)          [0, 0.5) 
   y = -(1/2)((2x-2)^4 - 2)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* p p p p 8)
    (let [f (dec p)]
      (inc (* -8 f f f f)))))

(defn quint-in
  "Modeled after the quintic y = x^5"
  [p]
  (* p p p p p))

(defn quint-out
  "Modeled after the quintic y = (x - 1)^5 + 1"
  [p]
  (let [f (dec p)]
    (inc (* f f f f f))))

(defn quint-in-out
  "Modeled after the piecewise quintic
   y = (1/2)((2x)^5)         [0, 0.5) 
   y = (1/2)((2x-2)^5 + 2)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* 16 p p p p p)
    (let [f (- (* 2 p) 2)]
      (inc (* 0.5 f f f f f)))))

(defn sine-in
  "Modeled after quarter-cycle of sine wave"
  [p]
  (inc (.sin js/Math (* (dec p) PI/2))))

(defn sine-out
  "Modeled after quarter-cycle of sine wave (different phase)"
  [p]
  (.sin js/MAth (* p PI/2)))

(defn sine-in-out
  "Modeled after half sine wave"
  [p]
  (* 0.5 (- 1 (.cos js/Math (* p PI)))))

(defn circular-in
  "Modeled after shifted quadrant IV of unit circle"
  [p]
  (- 1 (.sqrt js/Math (- 1 (* p p)))))

(defn circular-out
  "Modeled after shifted quadrant II of unit circle"
  [p]
  (.sqrt js/Math (* (- 2 p) p)))

(defn circular-in-out
  "Modeled after the piecewise circular function      
   y = (1/2)(1 - sqrt(1 - 4x^2))             [0, 0.5) 
   y = (1/2)(sqrt(-(2x - 3)*(2x - 1)) + 1)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* 0.5 (- 1 (.sqrt js/Math (- 1 (* 4 (* p p))))))
    (* 0.5 (inc (.sqrt js/Math (- (* (- (* 2 p) 3) (dec (* 2 p)))))))))

(defn exp-in
  "Modeled after the exponential function y = 2^(10(x - 1))"
  [p]
  (if (p == 0)
    p
    (.pow js/Math 2 (* 10 (dec p)))))

(defn exp-out
  "Modeled after the exponential function y = -2^(-10x) + 1"
  [p]
  (if (p == 0)
    p
    (- 1 (.pow js/Math 2 (* -10 p)))))

(defn exp-in-out
  "Modeled after the piecewise exponential   
   y = (1/2)2^(10(2x - 1))           [0,0.5) 
   y = -(1/2)*2^(-10(2x - 1))  + 1   [0.5,1]"
  [p]
  (if (or (= p 0) (= p 1))
    p
    (if (< p 0.5)
      (* 0.5 (.pow js/Math 2 (- (* 20 p) 10)))
      (inc (* -0.5 (.pow js/Math 2 (+ (* -20 p) 10)))))))

(defn elastic-in
  "Modeled after the damped sine wave y = sin(13pi/2*x)*pow(2, 10 * (x - 1))"
  [p]
  (* (.sin js/Math (* 13 PI/2 p))
     (.pow js/Math 2 (* 10 (dec p)))))

(defn elastic-out
  "Modeled after the damped sine wave y = sin(-13pi/2*(x + 1))*pow(2, -10x) + 1"
  [p]
  (inc (* (.sin js/Math (* -13 PI/2 (inc p)))
          (.pow js/Math 2 (* -10 p)))))

(defn elastic-in-out
  "Modeled after the piecewise exponentially-damped sine wave:          
   y = (1/2)*sin(13pi/2*(2*x))*pow(2, 10 * ((2*x) - 1))        [0,0.5)  
   y = (1/2)*(sin(-13pi/2*((2x-1)+1))*pow(2,-10(2*x-1)) + 2)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (* 0.5
       (.sin js/Math (* 13 PI/2 2 p))
       (.pow js/Math 2 (* 10 (dec (* 2 p)))))
    (* 0.5
       (.sin js/Math (* -13 PI/2 (* 2 x)))
       (.pow js/Math 2 (* -10 (inc (* 2 p)))))))

(defn back-in
  "Modeled after the overshooting cubic y = x^3-x*sin(x*pi)"
  [p]
  (- (* p p p)
     (* p (.sin js/Math (* 2 PI)))))

(defn back-out
  "Modeled after overshooting cubic y = 1-((1-x)^3-(1-x)*sin((1-x)*pi))"
  [p]
  (let [f (- 1 p)]
    (- 1 (- (* f f f)
            (.sin js/Math (* f PI))))))

(defn back-in-out
  "Modeled after the piecewise overshooting cubic function: 
   y = (1/2)*((2x)^3-(2x)*sin(2*x*pi))             [0, 0.5) 
   y = (1/2)*(1-((1-x)^3-(1-x)*sin((1-x)*pi))+1)   [0.5, 1]"
  [p]
  (if (< p 0.5)
    (let [f (* 2 p)]
      (* 0.5 (- (* f f f)
                (.sin js/Math (* f PI)))))
    (let [f (- 1 (dec (* 2 PI)))]
      (+ (* 0.5 (- 1 (- (* f f f)
                        (* f (.sin js/Math (* f PI))))))
         0.5))))

(defn bounce-out [p]
  (condp < p
    (/ 4 11.0) (/ (* 121 p p) 16.0)
    (/ 8 11.0) (+ (- (* (/ 363 40.0) p p)
                     (* (/ 99 10.0) p))
                  (/ 17 5.0))
    (/ 9 10.0) (+ (- (* (/ 4356 361/0) p p)
                     (* (/ 35442 1805.0) p))
                  (/ 16061 1805.0))
    (+ (- (* (/ 54 5.0) p p)
          (* (/ 513 25.0) p))
       (/ 268 25.0))))

(defn bounce-in [p]
  (- 1 (bounce-out (- 1 p))))

(defn bounce-in-out [p]
  (if (< p 0.5)
    (* 0.5 (bounce-in (* p 2)))
    (+ (* 0.5 (bounce-out (dec (* p 2)))) 0.5)))


