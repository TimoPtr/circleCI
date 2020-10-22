/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.mvi

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.guidedbrushing.R

@Keep
object GuidedBrushingTranslationKey {

    @StringRes
    @JvmField
    val TITLE = R.string.guided_brushing_title

    @StringRes
    @JvmField
    val MENU_HEADER = R.string.guided_brushing_pause_menu_header
    @StringRes
    @JvmField
    val MENU_RESUME = R.string.guided_brushing_pause_menu_resume_button
    @StringRes
    @JvmField
    val MENU_RESTART = R.string.guided_brushing_pause_menu_restart_button
    @StringRes
    @JvmField
    val MENU_QUIT = R.string.guided_brushing_pause_menu_quit_button

    @StringRes
    @JvmField
    val START_MESSAGE = R.string.guided_brushing_start_message

    @StringRes
    @JvmField
    val ERROR_MESSAGE = R.string.something_went_wrong

    @StringRes
    @JvmField
    val TIMER_SUBTITLE = R.string.guided_brushing_timer_subtitle

    @StringRes
    @JvmField
    val MOUTH_MAP_LEFT = R.string.guided_brushing_mouth_map_left
    @StringRes
    @JvmField
    val MOUTH_MAP_RIGHT = R.string.guided_brushing_mouth_map_right

    @StringRes
    @JvmField
    val LEGEND_CLEAN = R.string.guided_brushing_legend_clean
    @StringRes
    @JvmField
    val LEGEND_MISSED = R.string.guided_brushing_legend_missed
    @StringRes
    @JvmField
    val LEGEND_BRUSHING_SEGMENT = R.string.guided_brushing_legend_brushing_segment

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_ALL_GOOD = R.string.guided_brushing_no_feedback
    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_SPEED_FAST = R.string.guided_brushing_too_fast_feedback
    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_SPEED_SLOW = R.string.guided_brushing_too_slow_feedback
    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_ANGLE_INSIDE = R.string.guided_brushing_wrong_angle_feedback
    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_ZONE = R.string.guided_brushing_wrong_zone_feedback

    @StringRes
    @JvmField
    val LOST_CONNECTION_TITLE = R.string.dialog_lost_connection_title
    @StringRes
    @JvmField
    val LOST_CONNECTION_BODY = R.string.dialog_lost_connection_description
    @StringRes
    @JvmField
    val LOST_CONNECTION_BUTTON = R.string.dialog_lost_connection_quit_button
}
