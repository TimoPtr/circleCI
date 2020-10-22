/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.android.app.ui.home.tab.profile.SETTINGS
import com.kolibree.android.tracker.Analytics
import com.kolibree.android.tracker.AnalyticsEvent

@Suppress("TooManyFunctions")
internal object SettingsAdminEventTracker {

    fun main() = SETTINGS + ADMIN

    fun getMyData() = sendEvent(GET_MY_DATA)

    fun getMyDataSuccess() = sendEvent(GET_MY_DATA_SUCCESS)

    fun about() = sendEvent(ABOUT)

    fun help() = sendEvent(HELP)

    fun rateOurApp() = sendEvent(RATE_OUR_APP)

    fun term() = sendEvent(TERM)

    fun policy() = sendEvent(POLICY)

    fun guidedBrushing() = sendEvent(GUIDED_BRUSHING_SETTINGS)

    fun vibrationLevels() = sendEvent(VIBRATION_LEVEL)

    fun notifications() = sendEvent(NOTIFICATIONS)

    private fun sendEvent(screenName: String) = Analytics.send(main() + screenName)
}

private val ADMIN = AnalyticsEvent("Admin")

private const val GET_MY_DATA = "GetMyData"
private const val GET_MY_DATA_SUCCESS = "GetMyData_Success"
private const val ABOUT = "About"
private const val HELP = "Help"
private const val RATE_OUR_APP = "RateUs"
private const val TERM = "Term"
private const val POLICY = "Policy"
private const val GUIDED_BRUSHING_SETTINGS = "GBSetting"
private const val VIBRATION_LEVEL = "Vibration"
private const val NOTIFICATIONS = "Notification"
