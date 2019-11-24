(ns clarax.rules
  (:require [clara.rules :as rules]
            [clara.rules.engine :as engine]
            [clara.rules.accumulators])
  (:refer-clojure :exclude [merge]))

(defprotocol IMerge
  (merge [this fact new-args]))

(defprotocol ISession
  (session [this])
  (queries [this])
  (query-fns [this]))

(deftype Session [session queries query-fns]
  engine/ISession
  (insert [this facts]
    (-> (engine/insert session facts)
        (Session. queries query-fns)))
  (retract [this facts]
    (-> (engine/retract session facts)
        (Session. queries query-fns)))
  (fire-rules [this]
    (-> (engine/fire-rules session)
        (Session. queries query-fns)))
  (fire-rules [this opts]
    (-> (engine/fire-rules session opts)
        (Session. queries query-fns)))
  (query [this query-name params]
    (if (keyword? query-name)
      (if-let [query-fn (query-name query-fns)]
        (query-fn this params)
        (throw (ex-info (str "Query not found: " query-name) {})))
      (engine/query session query-name params)))
  (components [this]
    (engine/components session))
  
  IMerge
  (merge [this fact new-args]
    (-> session
        (rules/retract fact)
        (rules/insert (clojure.core/merge fact new-args))
        (Session. queries query-fns)))

  ISession
  (session [this] session)
  (queries [this] queries)
  (query-fns [this] query-fns))

(defn merge! [fact new-args]
  (rules/retract! fact)
  (rules/insert-unconditional! (clojure.core/merge fact new-args)))

