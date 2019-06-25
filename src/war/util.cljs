(ns war.util
  "Utility functions")

(defn map-invert*
  "Similar to map-invert. Takes a map with a collection of values for each key.
  Example: {:1 [:a :b :c]} => {:a :1 :b :1 :c :1}"
  [m]
  (into {} (for [[k vs] m
                 v      vs]
             [v k])))

(defn concatv
  "Like concat, but reverses arguments and returns a vector.
  Can only take two args."
  [x y]
  (vec (concat y x)))

(defn index-of [coll v]
  (let [i (count (take-while #(not= v %) coll))]
    (when (or (< i (count coll)) 
              (= v (last coll)))
      i)))
