(ns status-im.data-store.realm.schemas.account.v24.core
  (:require [status-im.data-store.realm.schemas.account.v22.chat :as chat]
            [status-im.data-store.realm.schemas.account.v1.chat-contact :as chat-contact]
            [status-im.data-store.realm.schemas.account.v19.contact :as contact]
            [status-im.data-store.realm.schemas.account.v20.discover :as discover]
            [status-im.data-store.realm.schemas.account.v24.message :as message]
            [status-im.data-store.realm.schemas.account.v12.pending-message :as pending-message]
            [status-im.data-store.realm.schemas.account.v1.processed-message :as processed-message]
            [status-im.data-store.realm.schemas.account.v19.request :as request]
            [status-im.data-store.realm.schemas.account.v19.user-status :as user-status]
            [status-im.data-store.realm.schemas.account.v5.contact-group :as contact-group]
            [status-im.data-store.realm.schemas.account.v5.group-contact :as group-contact]
            [status-im.data-store.realm.schemas.account.v8.local-storage :as local-storage]
            [status-im.data-store.realm.schemas.account.v21.browser :as browser]
            [goog.object :as object]
            [taoensso.timbre :as log]
            [cljs.reader :as reader]
            [clojure.string :as string]))

(def schema [chat/schema
             chat-contact/schema
             contact/schema
             discover/schema
             message/schema
             pending-message/schema
             processed-message/schema
             request/schema
             user-status/schema
             contact-group/schema
             group-contact/schema
             local-storage/schema
             browser/schema])

(defn update-clocks! [old-realm new-realm]
  (let [new-messages (.objects new-realm "message")
        old-messages (.objects old-realm "message")]
    (dotimes [i (.-length new-messages)]
      (let [new-message (aget new-messages i)
            old-message (aget old-messages i)
            timestamp  (aget old-message "timestamp")
            from-clock (aget old-message "from-clock-value")
            to-clock   (aget old-message "to-clock-value")]

        (aset new-message "clock-value" (+ (or from-clock 0) (or to-clock 0)))
        (aset new-message "received-timestamp" timestamp)))))

(defn migration [old-realm new-realm]
  (log/debug "migrating v24 account database: " old-realm new-realm)
  (update-clocks! old-realm new-realm))
