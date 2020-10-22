/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.lastbrushing

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object LastBrushingEventTracker {

    private fun main() = AnalyticsEvent(CHECKUP_HISTORY)

    private fun lastBrushing() = AnalyticsEvent(LAST_BRUSHING)

    private fun deleteBrushingSessionMain() = lastBrushing() + DELETE_BRUSHING_SESSION

    fun deleteBrushingSession() = send(deleteBrushingSessionMain())

    fun deleteBrushingSessionOk() = send(deleteBrushingSessionMain() + "Ok")

    fun deleteBrushingSessionCancel() = send(deleteBrushingSessionMain() + "No")
}

private const val CHECKUP_HISTORY = "CheckUp_History"
private const val LAST_BRUSHING = "LastBrushing"
private const val DELETE_BRUSHING_SESSION = "DeleteBrushingSession"
