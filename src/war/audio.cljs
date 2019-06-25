(ns war.audio
  "Audio stuff"
  (:require [re-frame.core :as rf]))

(def place-card
  (js/Audio. "audio/flip.mp3"))

(def win
  (js/Audio. "audio/win.mp3"))

(def lose
  (js/Audio. "audio/lose.mp3"))

(defn play
  [sound]
  (when-not @(rf/subscribe [:muted?])
    (.play sound)))


