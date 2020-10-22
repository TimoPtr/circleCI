/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.model

import android.net.Uri
import androidx.annotation.Keep

@Keep
interface CoachSettings {

    val enableBrushingMovement: Boolean
    val enableHelpText: Boolean
    val enableMusic: Boolean
    val enableShuffle: Boolean
    val profileId: Long
    val enableTransitionSounds: Boolean
    val musicUri: String?

    fun getUriOfMusic(): Uri =
        if (musicUri != null && !musicUri.isNullOrBlank()) Uri.parse(musicUri) else Uri.EMPTY

    fun updateEnableBrushingMovement(enable: Boolean): CoachSettings

    fun updateEnableHelpText(enable: Boolean): CoachSettings

    fun updateEnableMusic(enable: Boolean): CoachSettings

    fun updateEnableShuffle(enable: Boolean): CoachSettings

    fun updateEnableTransitionSounds(enable: Boolean): CoachSettings

    fun updateMusicUri(uri: String): CoachSettings

    companion object {

        fun create(profileId: Long): CoachSettings = CoachSettingsEntity(profileId = profileId)
    }
}
