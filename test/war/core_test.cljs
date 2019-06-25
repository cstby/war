(ns war.core-test
  (:require
   [cljs.test :refer-macros [deftest is testing are]]
   [war.cards :as cards]
   [war.logic :as logic]
   [war.view :as view]
   [war.util :as util]))

(deftest new-deck-count
  (is (= 52 (count cards/new-deck))))

(deftest card-values
  (are [value card] (= value (cards/value card))
    10 {:suit :hearts :rank :10}
    14 {:suit :hearts :rank :ace}
    13 {:suit :hearts :rank :king}
    12 {:suit :hearts :rank :queen}
    11 {:suit :hearts :rank :jack}))

(deftest names
  (are [short-name full-name] (= short-name (cards/to-name full-name))
    "h"  :hearts
    "s"  :spades
    "9"  :9
    "10" :10
    "1"  :ace
    "j"  :jack))

(deftest text
  (are [text card] (= text (cards/text card))
    "qh"  {:suit :hearts :rank :queen}
    "1s"  {:suit :spades :rank :ace}
    "10c" {:suit :club :rank :10}))

(deftest new-game
  (let [game-state (logic/new-game)]
    (is (= 52 (+ (count (:human-deck game-state))
                 (count (:computer-deck game-state)))))))

(deftest human-no-duplicates
  (let [cards         (logic/new-game)
        updated-cards (logic/human-play-card cards)]
    (is (= 26 (+ (count (:human-deck updated-cards))
                 (count (:human-stack updated-cards)))))))

(deftest computer-no-duplicates
  (let [cards         (logic/new-game)
        updated-cards (logic/computer-play-card cards)]
    (is (= 26 (+ (count (:computer-deck updated-cards))
                 (count (:computer-stack updated-cards)))))))

(deftest determine-winner-singles
  (let [cards {:human-stack    [{:suit :spades :rank :ace}]
               :computer-stack [{:suit :spades :rank :10}]}]
    (is (= (logic/determine-winner cards) :human-deck))))

(deftest determine-winner-tie
  (let [cards {:human-stack    [{:suit :spades :rank :ace}]
               :computer-stack [{:suit :hearts :rank :ace}]}]
    (is (= (logic/determine-winner cards) :tie))))

(deftest determine-winner-multi
  (let [cards {:human-stack    [{:suit :spades :rank :9}
                                {:suit :spades :rank :ace}]
               :computer-stack [{:suit :spades :rank :king}
                                {:suit :spades :rank :10}]}]
    (is (= (logic/determine-winner cards) :human-deck))))

(deftest give-cards
  (let [cards {:human-stack    [{:suit :spades :rank :9}
                                {:suit :spades :rank :ace}]
               :computer-stack [{:suit :spades :rank :king}
                                {:suit :spades :rank :10}]}]
    (is (= 4 (count (:human-deck (logic/give-cards-to-winner cards)))))))

(deftest map-invert*
  (is (= {:a :1 :b :1 :c :1}
         (util/map-invert* {:1 [:a :b :c]}))))

(deftest concatv
  (is (= [4 3 2 1]
         (util/concatv [2 1] [4 3]))))

#_(deftest card->pile
    (is (= (logic/card->pile {:suit :spades :rank :ace}) :human-stack)))

(deftest empty-stacks-true
  (is (= (logic/empty-stacks? (logic/new-game))
         true)))

(deftest empty-stacks-false
  (is (= (logic/empty-stacks? (logic/human-play-card (logic/new-game)))
         false)))

(deftest win?
  (is (= :win
         (logic/game-condition {:human-deck    [{:suit :spades :rank :king} {:suit :spades :rank :10}
                                                {:suit :spades :rank :9} {:suit :spades :rank :ace}]
                                :computer-deck []}))))

(deftest no-win-on-play
  (is (not= :win
            (logic/game-condition {:human-deck     [{:suit :spades :rank :king} {:suit :spades :rank :10}
                                                    {:suit :spades :rank :9} {:suit :spades :rank :ace}]
                                   :computer-deck  []
                                   :human-stack    [{:suit :spades :rank :king}]
                                   :computer-stack [{:suit :spades :rank :8}]}))))

#_(defn test-game
    "An arbitrary test game for debugging purposes."
    []
    {:human-deck     [{:suit :hearts :rank :6}
                      {:suit :hearts :rank :7}
                      {:suit :hearts :rank :8}
                      {:suit :hearts :rank :9}
                      {:suit :hearts :rank :10}
                      {:suit :hearts :rank :jack}
                      {:suit :hearts :rank :queen}
                      {:suit :hearts :rank :king}
                      {:suit :hearts :rank :ace}]
     :human-stack    []
     :computer-deck  [{:suit :spades :rank :8}
                      {:suit :spades :rank :9}
                      {:suit :spades :rank :10}
                      {:suit :spades :rank :jack}
                      {:suit :spades :rank :queen}
                      {:suit :spades :rank :king}
                      {:suit :spades :rank :ace}]
     :computer-stack []})
