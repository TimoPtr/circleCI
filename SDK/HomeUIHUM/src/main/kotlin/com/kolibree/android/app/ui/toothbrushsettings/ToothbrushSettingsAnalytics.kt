/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

@Suppress("TooManyFunctions")
internal object ToothbrushSettingsAnalytics {
    fun main() = AnalyticsEvent(name = "TBSettings")
    fun connectNewBrush() = send(main() + "NewBrush")
    fun buyNew() = send(main() + "BuyNew")
    fun resetCounter() = send(main() + "ResetCounter")
    fun identifyBrush() = send(main() + "BrushBlink")
    fun editName() = send(main() + "EditNickname")
    fun editNameSave() = send(main() + "EditNickname" + "Save")
    fun editNameCancel() = send(main() + "EditNickname" + "Cancel")
    fun notConnecting() = send(main() + "NotConnecting")
    fun help() = send(main() + "Help")
    fun forgetToothbrush() = send(main() + "ForgetBrush")
    fun forgetToothbrushYes() = send(main() + "ForgetBrush" + "Yes")
    fun forgetToothbrushCancel() = send(main() + "ForgetBrush" + "Cancel")
    fun ota() = send(main() + "BannerFirmwareUpdate")
    fun popupForgetBrush() = send(main() + "PopUp_Forget")
    fun popupForgetBrushCancel() = send(main() + "PopUp_Forget_Cancel")
    fun goBack() = send(main() + "GoBack")
}
