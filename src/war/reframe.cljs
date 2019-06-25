(ns war.reframe
  "Everything related to re-frame.
  Will probably split into multiple ns later."
  (:require [re-frame.core :as rf]
            [war.audio :as audio]
            [war.logic :as logic]))


;; subscriptions

(rf/reg-sub
 :muted?
 (fn [db _]
   (:muted? db)))

(rf/reg-sub
 :help?
 (fn [db _]
   (:help? db)))

(rf/reg-sub
 :cards
 (fn [db _]
   (:cards db)))

;; event handlers

(rf/reg-event-db 
 :initialize                 
 (fn [_ _]                   
   {:cards  (logic/new-game)
    :muted? false
    :help?  false}))

(rf/reg-event-db              
 :toggle-mute
 (fn [db]
   (update db :muted? not)))

(rf/reg-event-db              
 :toggle-help
 (fn [db]
   (update db :help? not)))

(rf/reg-event-db              
 :new-game
 (fn [db]
   (assoc db :cards (logic/new-game))))

(rf/reg-event-fx              
 :human-play-card
 (fn [cofx]
   {:db         (update (:db cofx) :cards logic/human-play-card)
    ;; HACK (see `inject-sub`)
    :dispatch-n (list (when-not (= @(rf/subscribe [:cards])
                                   (logic/human-play-card @(rf/subscribe [:cards])))
                        [:play-card-sound]))}))

(rf/reg-event-fx              
 :computer-play-card
 (fn [{:keys [db] :as cofx}]
   {:db         (update (:db cofx) :cards logic/computer-play-card)
    ;; HACK (see `inject-sub`)
    :dispatch-n (list (when-not (= @(rf/subscribe [:cards])
                                   (logic/computer-play-card @(rf/subscribe [:cards])))
                        [:play-card-sound]))}))

(rf/reg-event-fx              
 :give-cards-to-winner
 (fn [{:keys [cards] :as cofx}]
   {:db         (update (:db cofx) :cards logic/give-cards-to-winner)
    ;; HACK (see `inject-sub`)
    :dispatch-n (list (when-not (or (= @(rf/subscribe [:cards])
                                       (logic/give-cards-to-winner @(rf/subscribe [:cards])))
                                    (= :war (logic/game-condition @(rf/subscribe [:cards])))) [:play-card-sound]))}))

(rf/reg-event-fx
 :play-card-sound
 (fn [cfx _]
   {:play-card-sound nil}))

(rf/reg-fx
 :play-card-sound
 (fn [_ _] 
   (audio/play audio/place-card)))

(rf/reg-event-fx
 :play-sound
 (fn [cfx [_ sound]]
   (audio/play sound)))

(rf/reg-event-fx              
 :play-card
 (fn [cofx [_ _]]
   {:dispatch-later [{:ms 0 :dispatch [:human-play-card]}
                     {:ms 1000 :dispatch [:computer-play-card]}
                     (when-not (logic/eminent-war? (:cards (:db cofx))) 
                       {:ms 2500 :dispatch [:give-cards-to-winner]})]}))
(rf/reg-event-fx              
 :war
 (fn [cofx [_ _]]
   {:dispatch-later [{:ms 0 :dispatch [:human-play-card]}
                     {:ms 500 :dispatch [:human-play-card]}
                     {:ms 1000 :dispatch [:human-play-card]}
                     {:ms 2000 :dispatch [:computer-play-card]}
                     {:ms 2500 :dispatch [:computer-play-card]}
                     {:ms 3000 :dispatch [:computer-play-card]}
                     {:ms 4000 :dispatch [:human-play-card]}
                     {:ms 5000 :dispatch [:computer-play-card]}
                     {:ms 6000 :dispatch [:give-cards-to-winner]}]}))



