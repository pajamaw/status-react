(ns status-im.ui.screens.wallet.onboarding.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.onboarding.styles :as styles]
            [status-im.react-native.resources :as resources]))

(defn onboarding []
  [react/view styles/root
   [react/view {:style styles/onboarding-image-container}
    [react/image {:source (:wallet-welcome resources/ui)
                  :style  styles/onboarding-image}]]
   [react/text {:style styles/onboarding-title}
    (i18n/label :t/wallet-onboarding-title)]
   [react/text {:style styles/onboarding-text}
    (i18n/label :t/wallet-onboarding-description)]
   [components.common/button
    {:button-style {:flex-direction   :row
                    :background-color (colors/alpha colors/black 0.1)
                    :margin-bottom    32}
     :label-style  {:color "white"}
     :on-press     #(re-frame/dispatch [:navigate-to :wallet-setup])
     :label        (i18n/label :t/wallet-onboarding-set-up)}]])