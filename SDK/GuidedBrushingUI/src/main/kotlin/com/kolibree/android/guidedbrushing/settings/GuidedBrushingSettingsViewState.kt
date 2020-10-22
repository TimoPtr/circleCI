/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import android.net.Uri
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GuidedBrushingSettingsViewState(
    val isDisplayBrushingMovementOn: Boolean = false,
    val isDisplayHelpTextsOn: Boolean = false,
    val isMusicOn: Boolean = false,
    val isTransitionSoundsOn: Boolean = false,
    private val musicUri: String? = null
) : BaseViewState {

    fun withSettings(settings: CoachSettings): GuidedBrushingSettingsViewState = copy(
        isDisplayBrushingMovementOn = settings.enableBrushingMovement,
        isDisplayHelpTextsOn = settings.enableHelpText,
        isMusicOn = settings.enableMusic,
        isTransitionSoundsOn = settings.enableTransitionSounds,
        musicUri = settings.musicUri
    )

    fun musicUri(): Uri = if (musicUri != null && !musicUri.isNullOrBlank()) {
        Uri.parse(musicUri)
    } else {
        Uri.EMPTY
    }

    companion object {
        fun initial() = GuidedBrushingSettingsViewState()
    }
}
