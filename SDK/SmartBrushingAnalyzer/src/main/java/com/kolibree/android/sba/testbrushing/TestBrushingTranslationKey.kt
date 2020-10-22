/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.sba.R

@Keep
object TestBrushingTranslationKey {

    /*
    Screen 1
    */

    /**
     * Text displayed in bubble
     */
    @StringRes
    @JvmField
    val INTRO_BRUSHING_TIP = R.string.test_brushing_tip
    @StringRes
    @JvmField
    val INTRO_INSTRUCTIONS_HEADER = R.string.test_brushing_lets_see_how_you_brush
    @StringRes
    @JvmField
    val INTRO_INSTRUCTIONS_1 = R.string.test_brushing_instructions_1
    @StringRes
    @JvmField
    val INTRO_INSTRUCTIONS_2 = R.string.test_brushing_instructions_2
    @StringRes
    @JvmField
    val INTRO_INSTRUCTIONS_3 = R.string.test_brushing_instructions_3
    @StringRes
    @JvmField
    val INTRO_BUTTON_OK = R.string.intro_start_test
    @StringRes
    @JvmField
    val INTRO_BUTTON_CANCEL = R.string.intro_do_later

    /*
    Screen 2
     */
    @StringRes
    @JvmField
    val SESSION_TOP_TEXT = R.string.test_brushing_session_electric_line1
    @StringRes
    @JvmField
    val SESSION_BOTTOM_TEXT = R.string.test_brushing_session_electric_line2

    /*
    Screen 3
     */
    @StringRes
    @JvmField
    val DURING_SESSION_STEP1_DESCRIPTION_TEXT = R.string.durring_session_description_step1
    @StringRes
    @JvmField
    val DURING_SESSION_STEP1_HIGHLIGHTED_TEXT = R.string.durring_session_highlighted_step1

    /*
    Screen 4
     */
    @StringRes
    @JvmField
    val DURING_SESSION_STEP2_DESCRIPTION_TEXT = R.string.durring_session_description_step2

    /*
    Screen 5
     */

    @StringRes
    @JvmField
    val DURING_SESSION_STEP3_DESCRIPTION_TEXT = R.string.durring_session_description_step3
    @StringRes
    @JvmField
    val DURING_SESSION_STEP3_HIGHLIGHTED_TEXT = R.string.durring_session_highlighted_step3
    @StringRes
    @JvmField
    val DURING_SESSION_STEP3_MANUAL_DESCRIPTION_TEXT =
            R.string.durring_session_description_step3_manual
    @StringRes
    @JvmField
    val DURING_SESSION_STEP3_MANUAL_HIGHLIGHTED_TEXT =
            R.string.durring_session_highlighted_step3_manual

    /*
    Screen 6
     */
    @StringRes
    @JvmField
    val DURING_SESSION_STEP4_DESCRIPTION_TEXT = R.string.durring_session_description_step4
    @StringRes
    @JvmField
    val DURING_SESSION_STEP4_HIGHLIGHTED_TEXT = R.string.durring_session_highlighted_step4

    /*
    Popup 7
     */
    @StringRes
    @JvmField
    val POPUP_TITLE = R.string.finish_brushing_dialog_title
    @StringRes
    @JvmField
    val POPUP_CONFIRM = R.string.finish_brushing_dialog_yes
    @StringRes
    @JvmField
    val POPUP_RESUME = R.string.finish_brushing_dialog_resume

    /*
    Screen 8
     */
    @StringRes
    @JvmField
    val OPTIMIZE_TITLE = R.string.optimize_your_brushing_analysis
    @StringRes
    @JvmField
    val OPTIMIZE_HANDEDNESS_QUESTION = R.string.which_hand_do_you_hold_your_toothbrush_with
    @StringRes
    @JvmField
    val OPTIMIZE_BRUSHINGS_PER_DAY_QUESTION = R.string.how_many_times_a_day_do_you_brush_your_teeth
    @StringRes
    @JvmField
    val OPTIMIZE_BUTTON = R.string.get_my_results

    /*
    Screen 9
     */
    @StringRes
    @JvmField
    val ANALYSIS_IN_PROGRESS_TITLE = R.string.analysis_in_progress
    @StringRes
    @JvmField
    val ANALYSIS_IN_PROGRESS_MESSAGE_1 = R.string.brushing_progress_msg_1
    @StringRes
    @JvmField
    val ANALYSIS_IN_PROGRESS_MESSAGE_2 = R.string.brushing_progress_msg_2
    @StringRes
    @JvmField
    val ANALYSIS_IN_PROGRESS_MESSAGE_3 = R.string.brushing_progress_msg_3
    @StringRes
    @JvmField
    val ANALYSIS_IN_PROGRESS_MESSAGE_4 = R.string.brushing_progress_msg_4

    /*
    Screen 10
     */
    @StringRes
    @JvmField
    val RESULTS_PAGE1_TITLE = R.string.results_analysis_successful_title
    @StringRes
    @JvmField
    val RESULTS_PAGE1_BODY = R.string.results_analysis_successful_body
    @StringRes
    @JvmField
    val RESULTS_PAGE1_HAND = R.string.hint_tap_to_change_view

    /*
    Screen 11
     */
    @StringRes
    @JvmField
    val RESULTS_PAGE2_TITLE = R.string.results_mouth_coverage_title
    @StringRes
    @JvmField
    val RESULTS_PAGE2_HINT = R.string.results_mouth_coverage_hint
    @StringRes
    @JvmField
    val RESULTS_PAGE2_BODY_PERFECT = R.string.mouth_coverage_perfect
    @StringRes
    @JvmField
    val RESULTS_PAGE2_BODY_GOOD = R.string.mouth_coverage_good
    @StringRes
    @JvmField
    val RESULTS_PAGE2_BODY_MEDIUM = R.string.mouth_coverage_medium
    @StringRes
    @JvmField
    val RESULTS_PAGE2_BODY_BAD = R.string.mouth_coverage_bad
    @StringRes
    @JvmField
    val RESULTS_PAGE2_BODY_NO_DATA = R.string.mouth_coverage_no_data
    @StringRes
    @JvmField
    val RESULTS_PAGE2_CLEAN = R.string.results_mouth_coverage_tooth_clean
    @StringRes
    @JvmField
    val RESULTS_PAGE2_DIRTY = R.string.results_mouth_coverage_tooth_dirty
    @StringRes
    @JvmField
    val RESULTS_PAGE2_SURFACE = R.string.dashboard_surface
    @StringRes
    @JvmField
    val RESULTS_PAGE2_DURATION = R.string.dashboard_duration

    /*
    Screen 12
     */
    @StringRes
    @JvmField
    val RESULTS_PAGE3_TITLE = R.string.speed_card_title
    @StringRes
    @JvmField
    val RESULTS_PAGE3_CARD_HINT = R.string.speed_card_hint
    @StringRes
    @JvmField
    val RESULTS_PAGE3_LEARN_MORE_DIALOG_TITLE = R.string.speed_learn_more_dialog_title
    @StringRes
    @JvmField
    val RESULTS_PAGE3_LEARN_MORE_DIALOG_BODY_TOP = R.string.speed_learn_more_dialog_body_top
    @StringRes
    @JvmField
    val RESULTS_PAGE3_LEARN_MORE_DIALOG_BODY_BOTTOM = R.string.speed_learn_more_dialog_body_bottom
    @StringRes
    @JvmField
    val RESULTS_PAGE3_LEARN_MORE_DIALOG_HELP = R.string.speed_learn_more_dialog_help
    @StringRes
    @JvmField
    val RESULTS_PAGE3_DESCRIPTION_BY_GROUPS = R.string.speed_description_by_groups
    @StringRes
    @JvmField
    val RESULTS_PAGE3_DESCRIPTION_AVERAGE = R.string.speed_description_average
    @StringRes
    @JvmField
    val RESULTS_PAGE3_DESCRIPTION_ALL_GOOD = R.string.speed_description_all_good
    @StringRes
    @JvmField
    val RESULTS_PAGE3_GROUP_OCCLUSAL = R.string.speed_group_occlusal
    @StringRes
    @JvmField
    val RESULTS_PAGE3_GROUP_OUTSIDE_MOLARS = R.string.speed_group_outside_molars
    @StringRes
    @JvmField
    val RESULTS_PAGE3_GROUP_INSIDE_MOLARS = R.string.speed_group_inside_molars
    @StringRes
    @JvmField
    val RESULTS_PAGE3_GROUP_OUTSIDE_INCISIVES = R.string.speed_group_outside_incisives
    @StringRes
    @JvmField
    val RESULTS_PAGE3_GROUP_INSIDE_INCISIVES = R.string.speed_group_inside_incisives
    @StringRes
    @JvmField
    val RESULTS_PAGE3_QUADRANT_TOP_LEFT = R.string.speed_quadrant_top_left
    @StringRes
    @JvmField
    val RESULTS_PAGE3_QUADRANT_TOP_RIGHT = R.string.speed_quadrant_top_right
    @StringRes
    @JvmField
    val RESULTS_PAGE3_QUADRANT_BOTTOM_LEFT = R.string.speed_quadrant_bottom_left
    @StringRes
    @JvmField
    val RESULTS_PAGE3_QUADRANT_BOTTOM_RIGHT = R.string.speed_quadrant_bottom_right
    @StringRes
    @JvmField
    val RESULTS_PAGE3_SPEED_PROPER = R.string.speed_proper
    @StringRes
    @JvmField
    val RESULTS_PAGE3_SPEED_TOO_SLOW = R.string.speed_too_slow
    @StringRes
    @JvmField
    val RESULTS_PAGE3_SPEED_TOO_FAST = R.string.speed_too_fast

    /*
    Popup 13
     */
    @StringRes
    @JvmField
    val READ_SCHEMA_TITLE = R.string.read_diagram_dialog_title
    @StringRes
    @JvmField
    val READ_SCHEMA_BODY = R.string.read_diagram_dialog_body
    @StringRes
    @JvmField
    val READ_SCHEMA_TOP = R.string.read_diagram_dialog_top
    @StringRes
    @JvmField
    val READ_SCHEMA_BOTTOM = R.string.read_diagram_dialog_bottom
    @StringRes
    @JvmField
    val READ_SCHEMA_RIGHT = R.string.read_diagram_dialog_right
    @StringRes
    @JvmField
    val READ_SCHEMA_LEFT = R.string.read_diagram_dialog_left

    /*
    Popup 14
     */
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
