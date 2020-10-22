/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.pairing.startscreen

import com.kolibree.android.app.ui.toothbrushsettings.ToothbrushSettingsAnalytics
import com.kolibree.android.tracker.Analytics.send

internal object PairingStartScreenAnalytics {
    fun onConnectBrushClicked() = send(ToothbrushSettingsAnalytics.main() + "PopUpConnectBrush")
    fun onShowShopClicked() = send(ToothbrushSettingsAnalytics.main() + "PopUpShop")
}
