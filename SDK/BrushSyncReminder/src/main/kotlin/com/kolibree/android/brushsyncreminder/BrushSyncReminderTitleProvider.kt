/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder

import android.content.Context
import androidx.annotation.StringRes
import com.kolibree.account.ProfileFacade
import com.kolibree.android.hum.brushsyncreminder.R
import javax.inject.Inject
import kotlin.math.abs
import kotlin.random.Random

internal class BrushSyncReminderTitleProvider @Inject constructor(
    private val profileFacade: ProfileFacade
) {
    fun title(context: Context, profileId: Long, nextInt: Int = Random.nextInt()): String {
        val titleRes = nextTitle(nextInt)
        val title = context.getString(titleRes)
        return context.getString(R.string.reminder_notification_body, userName(profileId), title)
    }

    @StringRes
    private fun nextTitle(nextInt: Int): Int {
        val size = possibleTitles.size
        val nextIndex = abs(nextInt) % size
        return possibleTitles[nextIndex]
    }

    private fun userName(profileId: Long): String {
        val profile = profileFacade.getProfile(profileId).blockingGet()
        return profile.firstName.trim()
    }
}

internal val possibleTitles = arrayListOf(
    R.string.brush_reminder_notification_body1,
    R.string.brush_reminder_notification_body2,
    R.string.brush_reminder_notification_body3,
    R.string.brush_reminder_notification_body4,
    R.string.brush_reminder_notification_body5,
    R.string.brush_reminder_notification_body6,
    R.string.brush_reminder_notification_body7,
    R.string.brush_reminder_notification_body8,
    R.string.brush_reminder_notification_body9,
    R.string.brush_reminder_notification_body10,
    R.string.brush_reminder_notification_body11,
    R.string.brush_reminder_notification_body12
)
