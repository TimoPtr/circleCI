/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush

import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class OtaUpdateEvent(
    @OtaUpdateAction @get:JvmName("action") val action: Int,
    @get:JvmName("progress") val progress: Int? = null,
    @StringRes val errorMessageId: Int? = null
) {

    fun isProgressCompleted(): Boolean {
        return progress != null && progress >= OTA_COMPLETED_PROGRESS
    }

    fun updateWithEvent(event: OtaUpdateEvent): OtaUpdateEvent = copy(
        action = event.action,
        errorMessageId = event.errorMessageId,
        progress = event.progress?.takeIf { it > 0 } ?: progress
    )

    companion object {
        @JvmStatic
        fun fromProgressiveAction(
            @OtaUpdateAction action: Int,
            progress: Int
        ): OtaUpdateEvent {
            return OtaUpdateEvent(action = action, progress = progress)
        }

        @JvmStatic
        fun fromAction(@OtaUpdateAction action: Int): OtaUpdateEvent {
            return OtaUpdateEvent(action = action)
        }

        @JvmStatic
        @JvmOverloads
        fun fromError(@StringRes errorMessageId: Int = 0): OtaUpdateEvent {
            return OtaUpdateEvent(
                action = OTA_UPDATE_ERROR,
                errorMessageId = errorMessageId
            )
        }
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    OTA_UPDATE_REBOOTING,
    OTA_UPDATE_ERROR,
    OTA_UPDATE_INSTALLING,
    OTA_UPDATE_COMPLETED,
    OTA_UPDATE_BLOCKED_NOT_CHARGING
)
@Keep
internal annotation class OtaUpdateAction

@Keep
const val OTA_COMPLETED_PROGRESS = 100

@Keep
const val OTA_UPDATE_REBOOTING = 0

@Keep
const val OTA_UPDATE_INSTALLING = 1

@Keep
const val OTA_UPDATE_ERROR = 2

@Keep
const val OTA_UPDATE_COMPLETED = 3

@Keep
const val OTA_UPDATE_BLOCKED_NOT_CHARGING = 4
