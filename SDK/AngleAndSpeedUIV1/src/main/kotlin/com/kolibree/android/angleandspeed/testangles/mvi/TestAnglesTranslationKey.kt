/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.angleandspeed.R

@Keep
object TestAnglesTranslationKey {

    @StringRes
    @JvmField
    val INTRO_HEADER = R.string.test_angles_intro_header
    @StringRes
    @JvmField
    val INTRO_BODY = R.string.test_angles_intro_body
    @StringRes
    @JvmField
    val INTRO_START_BUTTON = R.string.test_angles_intro_start_button
    @StringRes
    @JvmField
    val GAME_STATE_CORRECT = R.string.test_angles_state_correct
    @StringRes
    @JvmField
    val GAME_STATE_INCORRECT = R.string.test_angles_state_incorrect
    @StringRes
    @JvmField
    val GAME_MOLAR_HINT = R.string.test_angles_molar_hint
    @StringRes
    @JvmField
    val GAME_INCISOR_HINT = R.string.test_angles_incisor_hint
    @StringRes
    @JvmField
    val CONFIRMATION_HINT = R.string.test_angles_confirmation_hint
    @StringRes
    @JvmField
    val CONFIRMATION_HINT_HIGHLIGHT = R.string.test_angles_confirmation_hint_highlight
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
