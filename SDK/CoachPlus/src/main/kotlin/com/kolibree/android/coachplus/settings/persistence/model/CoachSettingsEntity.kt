/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coach_settings")
internal data class CoachSettingsEntity(
    @ColumnInfo(name = "enable_brushing_movement") override val enableBrushingMovement: Boolean = true,
    @ColumnInfo(name = "enable_help_text") override val enableHelpText: Boolean = true,
    @ColumnInfo(name = "enable_music") override val enableMusic: Boolean = false,
    @ColumnInfo(name = "enable_shuffle") override val enableShuffle: Boolean = false,
    @ColumnInfo(name = "profile_id") @PrimaryKey override val profileId: Long,
    @ColumnInfo(name = "enable_transition_sounds") override val enableTransitionSounds: Boolean = false,
    @ColumnInfo(name = "musicUri") override val musicUri: String? = ""
) : CoachSettings {

    override fun updateEnableBrushingMovement(enable: Boolean) =
        copy(enableBrushingMovement = enable)

    override fun updateEnableHelpText(enable: Boolean) =
        copy(enableHelpText = enable)

    override fun updateEnableMusic(enable: Boolean) =
        copy(enableMusic = enable)

    override fun updateEnableShuffle(enable: Boolean) =
        copy(enableShuffle = enable)

    override fun updateEnableTransitionSounds(enable: Boolean) =
        copy(enableTransitionSounds = enable)

    override fun updateMusicUri(uri: String) =
        copy(musicUri = uri)

    companion object {

        @JvmStatic
        fun empty() = CoachSettingsEntity(profileId = 0)

        fun from(settings: CoachSettings) = CoachSettingsEntity(
            enableBrushingMovement = settings.enableBrushingMovement,
            enableHelpText = settings.enableHelpText,
            enableMusic = settings.enableMusic,
            enableShuffle = settings.enableMusic,
            profileId = settings.profileId,
            enableTransitionSounds = settings.enableTransitionSounds,
            musicUri = settings.musicUri
        )
    }
}
