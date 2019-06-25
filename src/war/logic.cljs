(ns war.logic
  "Rules and logic specific to the card game War.
  This namespace contains only pure functions."
  (:require [war.cards :as cards]
            [war.util :as util]
            [re-frame.core :as rf]))

(defn new-game
  "Deals a single deck of cards evenly to the human and computer players."
  []
  (let [[pile-one pile-two] (cards/deal 2 (cards/shuffled-decks 1))]
    {:human-deck     pile-one
     :human-stack    []
     :computer-deck  pile-two
     :computer-stack []}))

(def cards-on-felt
  "All cards currently in the game."
  (apply concat (vals (new-game))))

(defn card->pile
  "Given a specific card, returns the pile
  where the card is stored."
  [card]
  (get (util/map-invert* @(rf/subscribe [:cards])) card))

(defn empty-stacks?
  "Returns true if both stacks are empty."
  [cards]
  (and (empty? (:computer-stack cards))
       (empty? (:human-stack cards))))

(defn empty-deck?
  "Returns true if either deck is empty."
  [cards]
  (or (empty? (:computer-deck cards))
      (empty? (:human-deck cards))))

(defn determine-winner
  "Compares the value of first card of both the human and computer
  stacks and returns the winner. When the stacks are unequal, the
  player who has more cards wins (to account for when a player runs
  out of cards during a war)."
  [{:keys [human-stack computer-stack human-deck computer-deck] :as cards}]
  (if (and (empty-deck? cards)
           (or (not= 1 (count (:computer-stack cards)))
               (not= 1 (count (:human-stack cards)))))
    (if (> (count human-deck)
           (count computer-deck))
      :human-deck
      :computer-deck)
    (let [top-val      (comp cards/value peek)
          computer-top (top-val computer-stack)
          human-top    (top-val human-stack)]
      (cond
        (= computer-top human-top) :tie
        (> computer-top human-top) :computer-deck
        :else                      :human-deck))))

(defn give-cards-to-winner
  "Empties both stacks and and adds those cards to the winner's deck.
  Returns input on tie."
  [{:keys [human-stack human-deck computer-stack computer-deck] :as cards}]
  (let [winner   (determine-winner cards)
        winnings (concat human-stack computer-stack)]
    (if (= winner :tie)
      cards
      (-> cards
          (update-in [:computer-stack] empty)
          (update-in [:human-stack] empty)
          (update-in [winner] util/concatv winnings)))))

(defn computer-play-card
  "Returns updated cards after the computer player moves the top card of
  its deck to its stack."
  [{:keys [computer-deck] :as cards}]
  (if (or (empty? (:computer-deck cards))
          (and (empty? (:human-deck cards)) (empty? (:human-stack cards)) ))
    (give-cards-to-winner cards)
    (-> cards
        (update :computer-stack conj (peek computer-deck))
        (assoc :computer-deck (pop computer-deck)))))

(defn human-play-card
  "Returns updated cards after the human player moves the top card of
  her deck to her stack."
  [{:keys [human-deck] :as cards}]
  (if (or (empty? (:human-deck cards))
          (and (empty? (:computer-deck cards)) (empty? (:computer-stack cards)) ))
    (give-cards-to-winner cards)
    (-> cards
        (update :human-stack conj (peek human-deck))
        (assoc :human-deck (pop human-deck)))))

(defn tie?
  "Returns true if there's currently a tie."
  [cards]
  (if (or (empty? (:computer-stack cards))
          (empty? (:human-stack cards)))
    false 
    (= :tie (determine-winner cards))))

(defn eminent-war?
  "Returns true if the next draw will result in war."
  [cards]
  (if (empty-deck? cards)
    false
    (let [top-val       (comp cards/value peek)
          human-deck    (:human-deck cards)
          computer-deck (:computer-deck cards) 
          computer-top  (top-val computer-deck)
          human-top     (top-val human-deck)]
      (= computer-top human-top))))

(defn win?
  "Returns true if the player has won."
  [cards]
  (and (empty? (:computer-deck cards))
       (empty? (:computer-stack cards))))

(defn lose?
  "Returns true if the player has lost."
  [cards]
  (and (empty? (:human-deck cards))
       (empty? (:human-stack cards))))

(defn game-condition
  "Returns the game condition."
  [cards]
  (cond
    (win? cards)          :win
    (lose? cards)         :lose
    (empty-stacks? cards) :play-card
    (tie? cards)          :war
    :else                 :unplayable))


(defn next-move
  "Runs the correct next action, considering the app-state."
  []
  (case (game-condition @(rf/subscribe [:cards]))
    :play-card #(rf/dispatch [:play-card])
    :war       #(rf/dispatch [:war])
    nil))
