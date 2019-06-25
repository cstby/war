(ns war.cards
  "Defines playing cards as data structure.")

(def suits
  [:hearts :spades :diamonds :clubs])

(def ranks
  [:2 :3 :4 :5 :6 :7 :8 :9 :10 :jack :queen :king :ace])

(def new-deck
  (for [s suits
        r ranks]
    {:rank r :suit s}))

(defn shuffled-decks
  "Returns `n` decks shuffled together."
  [n]
  (->> (cycle new-deck)
       (take (* n (count new-deck)))
       shuffle))

(defn value
  "Returns a integer value for the given card."
  [{:keys [rank] :as card}]
  (case rank
    :ace   14
    :2     2
    :3     3
    :4     4
    :5     5
    :6     6
    :7     7
    :8     8
    :9     9
    :10    10
    :jack  11
    :queen 12
    :king  13))

(defn to-name
  "Takes a rank or suit and returns its short name.
  Example: :hearts => \"h\"  "
  [x]
  (cond
    (= x :ace) "1" 
    (= x :10)  "10"
    :else      (first (name x))))

(defn text
  "For a given card, returns its text form.
  Examples: qc, 1d, 10h"
  [{:keys [suit rank] :as card}]
  (apply str (mapcat to-name [rank suit])))

(defn image-path
  "For the given card, returns the path to its image file.
  Example: img/qc.svg"
  [card]
  (str "img/" (text card) ".svg"))

(defn deal
  "Returns `n` evenly distributed piles of `cards`."
  [n cards]
  (->> (partition-all (quot (count cards) n) cards)
       (map vec)))
