(ns clarax.rules-test
  (:require [clojure.test :refer :all]
            [clarax.macros-java :refer [->session]]
            [clarax.rules :as clarax]
            [clara.rules :as clara]))

(defrecord Player [x y hp])
(defrecord Enemy [x y hp])

(deftest query-enemy
  (-> (->session {:get-enemy
                  (fn []
                    (let [enemy Enemy]
                      enemy))})
      (clara/insert (->Enemy 2 2 10))
      (clara/query :get-enemy)
      :x
      (= 2)
      is))

(deftest query-enemies
  (-> (->session {:get-enemies
                  (fn []
                    (let [enemy [Enemy]]
                      enemy))})
      (clara/insert (->Enemy 0 0 10))
      (clara/insert (->Enemy 1 1 10))
      (clara/insert (->Enemy 2 2 10))
      (clara/query :get-enemies)
      count
      (= 3)
      is))

(deftest query-multiple-facts
  (as-> (->session {:get-enemy
                    (fn []
                      (let [enemy Enemy]
                        enemy))
                    :get-entities
                    (fn []
                      (let [enemy Enemy
                            player Player]
                        [enemy player]))})
        $
        (clara/insert $ (->Enemy 0 0 10))
        (clarax/merge $ (clara/query $ :get-enemy) {:x 1 :y 1})
        (clarax/merge $ (clara/query $ :get-enemy) {:x 2 :y 2})
        (clara/insert $ (->Player 3 3 10))
        (clara/query $ :get-entities)
        (let [[enemy player] $]
          (is (= (:x enemy) 2))
          (is (= (:x player) 3)))))

(deftest query-condition
  (-> (->session {:get-enemies
                  (fn []
                    (let [enemy [Enemy]
                          :when (>= (:x enemy) 1)]
                      enemy))})
      (clara/insert (->Enemy 0 0 10))
      (clara/insert (->Enemy 1 1 10))
      (clara/insert (->Enemy 2 2 10))
      (clara/query :get-enemies)
      count
      (= 2)
      is))

(deftest query-parameter
  (-> (->session {:get-enemy
                  (fn [?x ?y]
                    (let [player Player
                          enemy Enemy
                          :when (and (= (:x enemy) ?x)
                                     (= (:y enemy) ?y))]
                      enemy))})
      (clara/insert (->Enemy 1 0 10))
      (clara/insert (->Player 3 3 10))
      (clara/query :get-enemy :?x 1 :?y 0)
      record?
      is))

(deftest query-and-rule
  (as-> (->session {:get-player
                    (fn []
                      (let [player Player]
                        player))
                    :get-enemy
                    (fn []
                      (let [enemy Enemy]
                        enemy))
                    :hurt-enemy
                    (let [player Player
                          enemy Enemy
                          :when (and (= (:x player) (:x enemy))
                                     (= (:y player) (:y enemy)))]
                      (clarax/merge! player {:x (inc (:x player))})
                      (clarax/merge! enemy {:hp (dec (:hp enemy))}))})
        $
        (clara/insert $ (->Enemy 0 0 10))
        (clara/insert $ (->Player 3 3 10))
        (clarax/merge $ (clara/query $ :get-player) {:x 0 :y 0})
        (clarax/merge $ (clara/query $ :get-player) {:x 0 :y 10})
        (let [{:keys [hp]} (clara/query $ :get-enemy)]
          (is (= hp 9)))))
