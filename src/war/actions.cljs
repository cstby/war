(ns war.actions
  "Stateful and/or timed aggregrate actions"
  (:require [war.cards :as cards]
            [war.audio :as audio]
            [war.logic :as logic]
            [re-frame.core :as rf]))

#_(defn timed-play
    "Wrapper for timed app-state change."
    [app-state function timeout]
    (js/setTimeout #(do (audio/play app-state audio/place-card)
                        (swap! app-state update-in [:cards] function)) timeout))

#_(defn play-card
    "Plays the human card, then the computer card. Gives the cards to the
  winner unless the draw resulted in war."
    [app-state]
    (do (timed-play app-state logic/human-play-card 0)
        (timed-play app-state logic/computer-play-card 1000)
        (when (not (logic/eminent-war? (:cards @app-state)))
          (timed-play app-state logic/give-cards-to-winner 2500))))

#_(defn play-card
    "Plays the human card, then the computer card. Gives the cards to the
  winner unless the draw resulted in war."
    []
    (do (timed-play app-state logic/human-play-card 0)
        (timed-play app-state logic/computer-play-card 1000)
        (when (not (logic/eminent-war? (:cards @app-state)))
          (timed-play app-state logic/give-cards-to-winner 2500))))

#_(defn war!
    "Plays three cards for each player, then one last card each. Then
  gives all cards to the winner."
    [app-state]
    (do (timed-play app-state logic/human-play-card 0)
        (timed-play app-state logic/human-play-card 500)
        (timed-play app-state logic/human-play-card 1000)
        (timed-play app-state logic/computer-play-card 2000)
        (timed-play app-state logic/computer-play-card 2500)
        (timed-play app-state logic/computer-play-card 3000)
        ;; this doesn't work because it's the app state that's passed in at the beginning.      
        (when-not (empty? (:human-deck (:cards @app-state)))
          (timed-play app-state logic/human-play-card 4000))
        (when-not (empty? (:computer-deck (:cards @app-state)))
          (timed-play app-state logic/computer-play-card 5000))
        (println (:cards @app-state))
        (when (not (logic/pre-eminent-war? (:cards @app-state)))
          (timed-play app-state logic/give-cards-to-winner 6000))))


#_(defn next-move
    "Runs the correct next action, considering the app-state."
    [app-state]
    (case (logic/game-condition (:cards @app-state))
      :play-card (play-card app-state)
      :war       (war! app-state)
      nil))

(defn next-move
  "Runs the correct next action, considering the app-state."
  []
  (case (logic/game-condition @(rf/subscribe [:cards]))
    :play-card #(rf/dispatch [:play-card])
    :war       #(rf/dispatch [:war])
    nil))
