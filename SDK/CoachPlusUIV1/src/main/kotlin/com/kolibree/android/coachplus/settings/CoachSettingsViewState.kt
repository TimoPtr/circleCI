/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

/**
 * Set the State of the coach sounds settings view
 */
internal data class CoachSettingsViewState(
    val actions: CoachSettingsState = CoachSettingsState.ACTION_NONE,
    val enableBrushingMovement: Boolean = false,
    val enableHelpText: Boolean = false
) {

    enum class CoachSettingsState {
        ACTION_NONE,
        ACTION_OPEN_SOUND_PAGE
    }
}
