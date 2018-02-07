(ns status-im.ui.screens.usage-data.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.accounts.events :as accounts]))

(handlers/register-handler-fx
  :help-improve-handler
  (fn [cofx [_ yes? address]]
    (merge (when yes?
             (accounts/account-update {:sharing-usage-data? true} cofx))
           {:dispatch-n [(when yes? [:register-mixpanel-tracking address])
                         [:account-finalized]]})))
