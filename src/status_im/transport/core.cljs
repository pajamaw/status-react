(ns status-im.transport.core
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.transport.message.core :as message]
            [status-im.transport.filters :as filters]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            status-im.transport.inbox
            [status-im.utils.handlers :as handlers]
            [status-im.transport.db :as transport.db]))

(defn initialize-offline-inbox [_]
  (when config/offline-inbox-enabled?
    (log/debug :init-offline-inbox)
    {:dispatch [:initialize-offline-inbox]}))

(defn init-whisper
  [current-account-id {:keys [db web3] :as cofx}]
  (log/debug :init-whisper)
  (when-let [public-key (get-in db [:accounts/accounts current-account-id :public-key])]
    (handlers/merge-fx cofx
                       {:shh/add-discovery-filter {:web3           web3
                                                   :private-key-id public-key
                                                   :topic          (transport.utils/get-topic constants/contact-discovery)}
                        :shh/restore-sym-keys {:web3       web3
                                               :transport  (:transport/chats db)
                                               :on-success (fn [chat-id sym-key sym-key-id]
                                                             (re-frame/dispatch [::sym-key-added {:chat-id    chat-id
                                                                                                  :sym-key    sym-key
                                                                                                  :sym-key-id sym-key-id}]))}}
                       (initialize-offline-inbox))))

;;TODO (yenda) remove once go implements persistence
;;Since symkeys are not persisted, we restore them via add sym-keys,
;;this is the callback that is called when a key has been restored for a particular chat.
;;it saves the sym-key-id in app-db to send messages later
;;and starts a filter to receive messages
(handlers/register-handler-fx
  ::sym-key-added
  (fn [{:keys [db]} [_ {:keys [chat-id sym-key sym-key-id]}]]
    (let [web3 (:web3 db)
          {:keys [topic] :as chat} (get-in db [:transport/chats chat-id])]
      {:db (assoc-in db [:transport/chats chat-id :sym-key-id] sym-key-id)
       :data-store.transport/save {:chat-id chat-id
                                   :chat    (assoc chat :sym-key-id sym-key-id)}
       :shh/add-filter {:web3       web3
                        :sym-key-id sym-key-id
                        :topic      topic
                        :chat-id    chat-id}})))

;;TODO (yenda) uncomment and rework once go implements persistence
#_(doseq [[chat-id {:keys [sym-key-id topic] :as chat}] transport]
    (when sym-key-id
      (filters/add-filter! web3
                           {:symKeyID sym-key-id
                            :topics   [topic]}
                           (fn [js-error js-message]
                             (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id])))))

(defn stop-whisper [{:keys [db]}]
  (let [{:transport/keys [chats discovery-filter]} db
        chat-filters                               (mapv :filter (vals chats))
        all-filters                                (conj chat-filters discovery-filter)]
    {:shh/remove-filters all-filters}))
