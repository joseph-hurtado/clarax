(ns pixel-midi-gogo.app
  (:require [pixel-midi-gogo.core :refer [Def ->Def]]
            [pixel-midi-gogo.view :refer [View ->View]]
            [pixel-midi-gogo.event :refer [Event]]
            [clara.rules :as rules]
            [clara.rules.accumulators :refer [all]])
  (:require-macros [pixel-midi-gogo.core :as pmg]))

(defrecord TodoItem [text])

(defn init []
  (pmg/init
    [pixel-midi-gogo.core pixel-midi-gogo.view pixel-midi-gogo.event]
    ["dev-resources/pixel_midi_gogo/app.edn"]))

(init)

