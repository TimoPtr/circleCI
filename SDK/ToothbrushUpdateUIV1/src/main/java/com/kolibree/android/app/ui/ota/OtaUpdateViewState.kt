/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.kolibree.android.toothbrushupdate.R

/**
 * Used by BtTester
 */
@Keep
@Suppress("TooManyFunctions")
data class OtaUpdateViewState(
    val mandatoryUpdate: Boolean,
    val isRechargeable: Boolean,
    @OtaActionId @get:JvmName("otaActionId") val otaActionId: Int = OTA_ACTION_NONE,
    /**
     * Get the current actionText progression in percentText
     *
     * @return PERCENTAGE_UNDEFINED if the actionText duration is undefined, [0, 100] percentage
     * otherwise
     */
    @get:JvmName("otaActionProgressPercentage") val otaActionProgressPercentage: Int = PERCENTAGE_UNDEFINED,
    @get:JvmName("errorMessage") val errorMessage: String? = null,
    @StringRes @get:JvmName("message") val message: Int = 0,
    @get:JvmName("isActionButtonDisplayed") val isActionButtonDisplayed: Boolean = false,
    @get:JvmName("isCancelButtonDisplayed") val isCancelButtonDisplayed: Boolean = false,
    @get:JvmName("isProgressVisible") val isProgressVisible: Boolean = false,
    @get:JvmName("showNeedChargingDialog")val showNeedChargingDialog: Boolean = false
) {

    fun empty() = copy(
        otaActionId = OTA_ACTION_NONE,
        message = initialMessage(isRechargeable),
        isActionButtonDisplayed = true,
        isCancelButtonDisplayed = !mandatoryUpdate
    )

    fun checkingPrerequisite() = empty().copy(
        isProgressVisible = true
    )

    fun checkPrerequisiteComplete() = empty()

    fun blockedNotCharging() = empty().copy(
        showNeedChargingDialog = true
    )
    /**
     * Create a [OtaUpdateViewState] with undefined progress
     *
     * @param actionId @[OtaActionId]
     * @return non null [OtaUpdateViewState]
     */
    fun withUndefinedProgress(@OtaActionId actionId: Int) = copy(
        otaActionId = actionId,
        message = initialMessage(isRechargeable)
    )

    /**
     * Create a [OtaUpdateViewState] with actionText progression
     *
     * @param actionId @[OtaActionId]
     * @param progress [0, 100] progress percentage
     * @return non null [OtaUpdateViewState]
     */
    fun withProgress(@OtaActionId actionId: Int, progress: Int) = copy(
        otaActionId = actionId,
        otaActionProgressPercentage = progress,
        message = initialMessage(isRechargeable)
    )

    fun withOtaError(errorMessage: String?) = copy(
            otaActionId = OTA_ACTION_ERROR,
            otaActionProgressPercentage = PERCENTAGE_UNDEFINED,
            errorMessage = errorMessage
        )

    fun withUpdateCompleted() = copy(
            otaActionId = OTA_ACTION_COMPLETED,
            message = R.string.firmware_success,
            isActionButtonDisplayed = true
        )

    fun withUpdateFinished(@OtaActionId actionId: Int) = copy(
            otaActionId = actionId
        )

    fun withConfirmExit() = copy(
            otaActionId = OTA_CONFIRM_EXIT,
            isCancelButtonDisplayed = true
        )

    @StringRes
    fun initialMessage(isRechargeable: Boolean) =
        if (isRechargeable) R.string.firmware_upgrade_welcome
        else R.string.firmware_upgrade_welcome_nonrechargeable

    companion object {
        @JvmStatic
        fun init(mandatoryUpdate: Boolean = false, isRechargeable: Boolean = false) =
            OtaUpdateViewState(mandatoryUpdate, isRechargeable)
    }
}

@kotlin.annotation.Retention
@IntDef(
    OTA_ACTION_ERROR,
    OTA_ACTION_REBOOTING,
    OTA_ACTION_INSTALLING,
    OTA_ACTION_NONE,
    OTA_ACTION_COMPLETED,
    OTA_CONFIRM_EXIT,
    OTA_ACTION_EXIT_SUCCESS,
    OTA_ACTION_EXIT_CANCEL
)
internal annotation class OtaActionId

@Keep
const val OTA_ACTION_ERROR = -1

@Keep
const val OTA_ACTION_REBOOTING = 0

@Keep
const val OTA_ACTION_INSTALLING = 2

@Keep
const val OTA_ACTION_COMPLETED = 5

@Keep
const val OTA_ACTION_NONE = 6

@Keep
const val OTA_ACTION_EXIT_SUCCESS = 7

@Keep
const val OTA_ACTION_EXIT_CANCEL = 8

@Keep
const val OTA_CONFIRM_EXIT = 9

/** Subscriber view should display a undetermined state progress  */
internal const val PERCENTAGE_UNDEFINED = -1
