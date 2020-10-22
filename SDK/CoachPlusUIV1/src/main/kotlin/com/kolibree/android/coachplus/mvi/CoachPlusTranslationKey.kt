/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.mvi

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.coachplus.R

@Keep
object CoachPlusTranslationKey {

    @StringRes
    @JvmField
    val TITLE = R.string.coach_plus_title

    @StringRes
    @JvmField
    val MENU_HEADER = R.string.pause

    @StringRes
    @JvmField
    val MENU_RESUME = R.string.resume

    @StringRes
    @JvmField
    val MENU_RESTART = R.string.restart

    @StringRes
    @JvmField
    val MENU_QUIT = R.string.quit

    @StringRes
    @JvmField
    val START_MESSAGE_ELECTRIC = R.string.coach_start_message_title

    @StringRes
    @JvmField
    val START_MESSAGE_MANUAL = R.string.coach_start_message_title_manual

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_INCISOR_EXTERIOR = R.string.mouth_zone_bottom_incisor_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_INCISOR_INTERIOR = R.string.mouth_zone_bottom_incisor_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_LEFT_EXTERIOR = R.string.mouth_zone_bottom_molar_left_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_LEFT_INTERIOR = R.string.mouth_zone_bottom_molar_left_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_LEFT_OCCLUSAL = R.string.mouth_zone_bottom_molar_left_occlusal

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_RIGHT_OCCLUSAL = R.string.mouth_zone_bottom_molar_right_occlusal

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_RIGHT_EXTERIOR = R.string.mouth_zone_bottom_molar_right_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_BOTTOM_MOLAR_RIGHT_INTERIOR = R.string.mouth_zone_bottom_molar_right_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_INCISOR_EXTERIOR = R.string.mouth_zone_top_incisor_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_INCISOR_INTERIOR = R.string.mouth_zone_top_incisor_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_LEFT_EXTERIOR = R.string.mouth_zone_top_molar_left_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_LEFT_INTERIOR = R.string.mouth_zone_top_molar_left_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_LEFT_OCCLUSAL = R.string.mouth_zone_top_molar_left_occlusal

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_RIGHT_EXTERIOR = R.string.mouth_zone_top_molar_right_exterior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_RIGHT_INTERIOR = R.string.mouth_zone_top_molar_right_interior

    @StringRes
    @JvmField
    val MOUTH_ZONE_TOP_MOLAR_RIGHT_OCCLUSAL = R.string.mouth_zone_top_molar_right_occlusal

    @StringRes
    @JvmField
    val ERROR_MESSAGE = R.string.coach_something_wrong

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_SPEED_FAST = R.string.coach_plus_feedback_message_speed_fast

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_SPEED_SLOW = R.string.coach_plus_feedback_message_speed_slow

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_ANGLE_INSIDE =
        R.string.coach_plus_feedback_message_wrong_angle_incisors_interior

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_ANGLE_OTHER_ZONES =
        R.string.coach_plus_feedback_message_wrong_angle_other_zones

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_OVERPRESSURE =
        R.string.coach_plus_feedback_message_overpressure

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_HANDLE = R.string.coach_plus_feedback_message_wrong_handle

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_WRONG_ANGLE_INCISORS_INTERIOR =
        R.string.coach_plus_feedback_message_wrong_angle_incisors_interior

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_OUT_OF_MOUTH = R.string.coach_plus_feedback_message_out_of_mouth

    @StringRes
    @JvmField
    val FEEDBACK_MESSAGE_RINSE_BRUSH_HEAD = R.string.coach_plus_feedback_message_rinse_brush_head

    @StringRes
    @JvmField
    val SETTINGS_CHOOSE_MUSIC = R.string.coach_settings_choose_music

    @StringRes
    @JvmField
    val SETTINGS_COACH_SOUNDS_TITLE = R.string.coach_settings_coach_sounds_title

    @StringRes
    @JvmField
    val SETTINGS_SHUFFLE = R.string.coach_settings_shuffle

    @StringRes
    @JvmField
    val SETTINGS_MUSIC = R.string.coach_settings_music

    @StringRes
    @JvmField
    val SETTINGS_TRANSITIONS_SOUNDS = R.string.coach_settings_transitions_sounds

    @StringRes
    @JvmField
    val SETTINGS_DISPLAY_BRUSHING_MOVEMENTS = R.string.coach_settings_display_brushing_movements

    @StringRes
    @JvmField
    val SETTINGS_DISPLAY_HELPS_TEXTS = R.string.coach_settings_display_helps_texts

    @StringRes
    @JvmField
    val SETTINGS_SOUNDS = R.string.coach_settings_sounds

    @StringRes
    @JvmField
    val NO_MUSIC_FILE = R.string.no_music_file

    @StringRes
    @JvmField
    val LOST_CONNECTION_TITLE = R.string.dialog_lost_connexion_title

    @StringRes
    @JvmField
    val LOST_CONNECTION_BODY = R.string.dialog_lost_connexion_description

    @StringRes
    @JvmField
    val LOST_CONNECTION_BUTTON = R.string.dialog_lost_connexion_quit_btn

    @StringRes
    @JvmField
    val BRUSHING_PROGRAM_DIALOG_POLISHING = R.string.brushing_program_dialog_polishing

    @StringRes
    @JvmField
    val BRUSHING_PROGRAM_DIALOG_CUSTOM = R.string.brushing_program_dialog_custom
}
