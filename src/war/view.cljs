(ns war.view
  "Components to be rendered."
  (:require [clojure.pprint :refer [pprint]]
            #_[war.actions :as actions]
            [war.actions :as actions]
            [war.audio :as audio]
            [war.cards :as cards]
            [war.logic :as logic]
            [war.util :as util]
            [re-frame.core :as rf]))


(def pile->offsets
  "For each pile, a map of info to render cards.
  - x position of the pile
  - y position of the pile
  - the difference in x position for each additional card in the pile
  - the difference in y position for each additional card in the pile
  - whether the cards in the pile are face up (when true)
  - whether the cards should be slightly rotated.  "
  {:computer-deck  {:x 0 :y 20 :xd -0.05 :yd -0.05 :up? false}
   :human-deck     {:x 0 :y 80 :xd -0.05 :yd -0.05 :up? false}
   :computer-stack {:x -10 :y 50 :xd -2 :yd 0 :up? true}
   :human-stack    {:x 10 :y 50 :xd 2 :yd 0 :up? true}})

(defn card->position
  "In the current state of the app, for the given card, return what that card's position should be."
  [card]
  (let [pile                    (logic/card->pile card)
        offsets                 (pile->offsets pile)
        index                   (util/index-of (to-array (pile @(rf/subscribe [:cards]))) card)
        {:keys [x y xd yd up?]} offsets]
    {:x        (+ x (* index xd))
     :y        (+ y (* index yd))
     :z        index
     :showing? up?}))

(defn render-card
  "Takes a card and a position and returns the card as a component."
  [card position] 
  [:img 
   {:src   (if (:showing? position)
             (cards/image-path card)
             "img/Sun Flower.png")
    :style {:height        "20%" 
            :border-radius 5
            :box-shadow    "0 1px 1px rgba(0,0,0,0.25)"
            :position      "absolute"
            :left          (str "calc(50% + " (:x position) "vh)") 
            :top           (str (:y position) "%")
            :z-index       (:z position)
            :transform     "translate(-50%, -50%) " 
            :transition    "top 1s ease, left 1s ease, z-index 0s 0.35s"}}])

(defn render-cards
  "Renders all cards currently in the game."
  []
  [:div {:style    {:position "absolute"
                    :width    "100%"
                    :height   "calc(100% - 1.75rem)"
                    :z-index  "auto"}
         :on-click (logic/next-move)}
   (doall (->> logic/cards-on-felt 
               (map (juxt identity card->position))
               (map (fn [[c p]] ^{:key c} [render-card c p]))))])

(defn button
  "Button component"
  [text function]
  [:button.button
   {:on-click function}
   text])

(defn image-button
  [source on-click-event]
  [:img
   {:src      source
    :style    {:height         "1.5rem"
               :padding        "0.15rem"
               :vertical-align "top"
               :cursor         "pointer"
               :margin-right   "1rem"}
    :on-click #(rf/dispatch [on-click-event])}])


(defn mute-button
  []
  [image-button
   (if @(rf/subscribe [:muted?]) "img/muted.svg" "img/unmuted.svg")
   :toggle-mute])

(defn help-button
  []
  [image-button
   "img/help.svg"
   :toggle-help])

(defn outcome
  "Dialog for win state."
  [outcome-text]
  [:div {:style {:top              "50%"
                 :left             "50%"
                 :transform        "translate(-50%, -50%)"
                 :position         "absolute"
                 :background-color "#333333"
                 :color            "white" 
                 :opacity          "0.9"
                 :width            "40%"
                 :z-index          500
                 :padding          "1rem"
                 :font-size        "2.75rem"
                 :overflow         "hidden"
                 :border-radius    10
                 :text-align       "center"}}
   [:div outcome-text]
   [button "New Game"
    #(rf/dispatch [:new-game])]])

(defn help-text
  []
  [:div
   [:h1 {:style {:text-align "center"}}
    "War!"]
   [:h2 "How to play:"]
   [:p " Click anywhere to play the top card from your deck. Your
   opponent will do the same. The player with the highest card takes
   both cards and adds them to the bottom of her deck."
    [:p "When the two cards are tied, it's war! Both you and your
    opponent will play three cards, then one final card each. The
    player whose top card ranks highest will take all the cards and
    them them to their deck."]]])

(defn help
  "Dialog for help."
  []
  (when @(rf/subscribe [:help?])
    [:div {:style    {:top              "50%"
                      :left             "50%"
                      :transform        "translate(-50%, -50%)"
                      :position         "absolute"
                      :background-color "#333333"
                      :color            "white" 
                      :opacity          "0.90"
                      :width            "40%"
                      :height           "50%"
                      :z-index          500
                      :padding          "0.25rem 5% 0.25rem 5%"
                      :font-size        "1rem"
                      :overflow         "hidden"
                      :border-radius    10
                      :overflow-y       "auto"}
           :on-click #(rf/dispatch [:toggle-help])}
     [help-text]]))

(defn game-over?
  ""
  []
  (case (logic/game-condition @(rf/subscribe [:cards]))
    :win  [:div [outcome "You Win!"]
           [#(rf/dispatch [:play-sound audio/win])]]
    :lose [:div [outcome "You Lose!"]
           [#(rf/dispatch [:play-sound audio/lose])]]
    nil))

(defn win-lose
  []
  [#(case @(rf/subscribe [:help?])
      true (rf/dispatch [:play-win-sound])
      nil)])


(defn top-bar []
  [:div {:style {:top              0
                 :left             0
                 :position         "static"
                 :background-color "#333333"
                 :opacity          "0.9"
                 :height           "1.75rem"
                 :padding          "0.25rem"
                 :overflow         "hidden"
                 :width            "100%"
                 :text-align       "right"}}
   [mute-button]
   [help-button]])

(defn war []
  [:div
   [game-over?]
   [help]
   [top-bar]
   [render-cards]
   [win-lose]])



