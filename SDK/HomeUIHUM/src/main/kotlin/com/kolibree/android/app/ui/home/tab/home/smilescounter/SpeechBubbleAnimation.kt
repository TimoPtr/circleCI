/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import android.content.Context
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import com.kolibree.android.homeui.hum.R

internal sealed class SpeechBubbleAnimation<T>(
    @StringRes protected val textRes: Int = 0,
    @IntegerRes protected val duration: Int = 0,
    @IntegerRes protected val startDelay: Int = 0,
    val value: T? = null
) {
    open fun getFormattedString(context: Context): String =
        textRes.takeIf { it > 0 }
            ?.let { context.getString(it) } ?: ""

    fun getStartDelay(context: Context): Long =
        startDelay.takeIf { it > 0 }
            ?.let { context.resources.getInteger(it).toLong() } ?: 0L

    fun getDuration(context: Context): Long =
        duration.takeIf { it > 0 }
            ?.let { context.resources.getInteger(it).toLong() } ?: 0L

    object Pending : SpeechBubbleAnimation<Void>(
        R.string.pending_label,
        R.integer.smiles_counter_pending_label_duration,
        R.integer.smiles_counter_pending_label_start_delay
    )

    class PointsIncrease(value: Int) : SpeechBubbleAnimation<Int>(
        R.string.points_increase,
        R.integer.smiles_counter_speech_bubble_duration,
        R.integer.smiles_counter_reels_duration,
        value
    ) {
        override fun getFormattedString(context: Context) =
            context.getString(textRes, value)
    }

    object NoInternet : SpeechBubbleAnimation<Void>(
        R.string.home_screen_no_internet,
        android.R.integer.config_shortAnimTime
    )

    object Hide : SpeechBubbleAnimation<Void>(duration = android.R.integer.config_shortAnimTime)
}
