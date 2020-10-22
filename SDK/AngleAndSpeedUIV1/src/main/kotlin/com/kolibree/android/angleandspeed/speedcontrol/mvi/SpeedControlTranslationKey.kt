/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.angleandspeed.R

@Keep
object SpeedControlTranslationKey {

    @StringRes
    @JvmField
    val INTRO_HEADER = R.string.speed_control_intro_header
    @StringRes
    @JvmField
    val INTRO_BODY = R.string.speed_control_intro_body
    @StringRes
    @JvmField
    val INTRO_START_BUTTON = R.string.speed_control_intro_start_button
    @StringRes
    @JvmField
    val CONFIRMATION_HINT = R.string.speed_control_confirmation_hint
    @StringRes
    @JvmField
    val CONFIRMATION_HINT_HIGHLIGHT = R.string.speed_control_confirmation_hint_highlight
    @StringRes
    @JvmField
    val BRUSHING_STAGE1_HINT = R.string.speed_control_brushing_stage1_hint
    @StringRes
    @JvmField
    val BRUSHING_STAGE1_HINT_HIGHLIGHT = R.string.speed_control_brushing_stage1_hint_highlight
    @StringRes
    @JvmField
    val BRUSHING_STAGE2_HINT = R.string.speed_control_brushing_stage2_hint
    @StringRes
    @JvmField
    val BRUSHING_STAGE2_HINT_HIGHLIGHT = R.string.speed_control_brushing_stage2_hint_highlight
    @StringRes
    @JvmField
    val BRUSHING_STAGE3_HINT = R.string.speed_control_brushing_stage3_hint
    @StringRes
    @JvmField
    val BRUSHING_STAGE3_HINT_HIGHLIGHT = R.string.speed_control_brushing_stage3_hint_highlight
    @StringRes
    @JvmField
    val FEEDBACK_UNDERSPEED = R.string.speed_control_feedback_underspeed
    @StringRes
    @JvmField
    val FEEDBACK_OVERSPEED = R.string.speed_control_feedback_overspeed
    @StringRes
    @JvmField
    val FEEDBACK_CORRECT = R.string.speed_control_feedback_correct
    @StringRes
    @JvmField
    val LOST_CONNECTION_TITLE = R.string.dialog_lost_connexion_title
    @StringRes
    @JvmField
    val LOST_CONNECTION_BODY = R.string.dialog_lost_connexion_description
    @StringRes
    @JvmField
    val LOST_CONNECTION_BUTTON = R.string.dialog_lost_connexion_quit_btn
}
