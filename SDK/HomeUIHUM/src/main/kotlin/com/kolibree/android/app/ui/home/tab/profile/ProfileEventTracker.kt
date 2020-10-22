/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile

import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

internal object ProfileEventTracker {
    private fun main() = SETTINGS

    private fun amazon() = main() + AMAZON

    fun changePicture() = sendEvent(CHANGE_PICTURE)

    fun goToSetting() = sendEvent(GO_TO_SETTING)

    fun logout() = sendEvent(LOGOUT)

    fun deleteAccount() = sendEvent(DELETE_ACCOUNT)

    fun weeklyDigest(isEnabled: Boolean) = Analytics.send(main() + WEEKLY_DIGEST + if (isEnabled) "ON" else "OFF")

    fun dataCollection(isEnabled: Boolean) = Analytics.send(main() + DATA_COLLECTION + if (isEnabled) "ON" else "OFF")

    fun dataCollectionInfo() = Analytics.send(main() + DATA_COLLECTION + INFO)

    fun linkAmazonDash() = Analytics.send(amazon() + "link")

    fun unlinkAmazonDashConfirmed() = Analytics.send(amazon() + "unlink_ok")

    fun unlinkAmazonDashCancel() = Analytics.send(amazon() + "unlink_cancel")

    private fun sendEvent(screenName: String) = Analytics.send(main() + screenName)
}

internal val SETTINGS = AnalyticsEvent("Setting")
private const val AMAZON = "Amazon"
private const val CHANGE_PICTURE = "ChangePicture"
private const val GO_TO_SETTING = "GoToSetting"
private const val LOGOUT = "LogOut"
private const val DELETE_ACCOUNT = "DeleteAccount"
private const val WEEKLY_DIGEST = "Weekly"
private const val DATA_COLLECTION = "Share"
private const val INFO = "Info"
