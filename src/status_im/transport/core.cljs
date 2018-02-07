(ns status-im.transport.core
  (:require [cljs.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.transport.message.core :as message]
            [status-im.transport.filters :as filters]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.config :as config]
            [taoensso.timbre :as log]
            status-im.transport.inbox))

(defn stop-whisper! []
  #_(stop-watching-all!))

(defn init-whisper!
  [{:keys [web3 identity transport]}]
  (log/debug :init-whisper)
  (let [filter-opts {:privateKeyID identity
                     :topics [(transport.utils/get-topic constants/contact-discovery)]}]
    (filters/add-filter! web3
                         filter-opts
                         (fn [js-error js-message]
                           (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message])))
    (when config/offline-inbox-enabled?
      (log/debug :init-offline-inbox)
      (re-frame/dispatch [:initialize-offline-inbox])))

  ;;TODO (yenda) uncomment and rework once go implements persistence
  #_(doseq [[chat-id {:keys [sym-key-id topic] :as chat}] transport]
      (when sym-key-id
        (filters/add-filter! web3
                             {:symKeyID sym-key-id
                              :topics [topic]}
                             (fn [js-error js-message]
                               (re-frame/dispatch [:protocol/receive-whisper-message js-error js-message chat-id]))))))
