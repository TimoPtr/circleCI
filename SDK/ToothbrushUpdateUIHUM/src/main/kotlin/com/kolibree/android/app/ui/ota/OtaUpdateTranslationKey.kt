/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
object OtaUpdateTranslationKey {

    /*
    Start ota screen
     */

    /**
     * Should contain START_OTA_TITLE_HIGHLIGHT
     */
    @StringRes
    @JvmField
    val START_OTA_TITLE = R.string.start_ota_title

    /**
     * Should be included in START_OTA_TITLE
     */
    @StringRes
    @JvmField
    val START_OTA_TITLE_HIGHLIGHT = R.string.start_ota_title_highlight

    @StringRes
    @JvmField
    val START_OTA_CONTENT = R.string.start_ota_content

    @StringRes
    @JvmField
    val START_OTA_SUB_CONTENT1 = R.string.start_ota_subcontent1

    @StringRes
    @JvmField
    val START_OTA_SUB_CONTENT2 = R.string.start_ota_subcontent2

    /**
     * This one is only displayed when the brush is rechargeable
     */
    @StringRes
    @JvmField
    val START_OTA_SUB_CONTENT3 = R.string.start_ota_subcontent3

    @StringRes
    @JvmField
    val START_OTA_UPGRADE = R.string.start_ota_upgrade

    /**
     * This message is an error message when E2 brush is not on charger and
     * FW is bellow a given version
     */
    @StringRes
    @JvmField
    val START_OTA_PUT_BRUSH_ON_CHARGER = R.string.start_ota_put_brush_on_charger

    @StringRes
    @JvmField
    val START_OTA_CANCEL_MANDATORY = R.string.start_ota_cancel_mandatory

    @StringRes
    @JvmField
    val START_OTA_CANCEL = R.string.cancel

    /*
    In progress screen
    */

    @StringRes
    @JvmField
    val IN_PROGRESS_OTA_TITLE = R.string.in_progress_ota_title

    @StringRes
    @JvmField
    val IN_PROGRESS_OTA_CONTENT = R.string.in_progress_ota_content

    /**
     * It should contain %d the progress will be inject in it
     */
    @StringRes
    @JvmField
    val IN_PROGRESS_OTA_PROGRESS = R.string.in_progress_ota_progress

    @StringRes
    @JvmField
    val OTA_DONE_TITLE = R.string.ota_done_title

    @StringRes
    @JvmField
    val OTA_DONE_CONTENT = R.string.ota_done_content

    @StringRes
    @JvmField
    val OTA_DONE = R.string.ota_done

    /*
    Failure screen
     */

    @StringRes
    @JvmField
    val OTA_FAILURE_TITLE = R.string.ota_failure_title

    @StringRes
    @JvmField
    val OTA_FAILURE_CONTENT = R.string.ota_failure_content

    /*
    Error message
     */

    @StringRes
    @JvmField
    val OTA_BLOCKER_NO_ACTIVE_CONNECTION = R.string.ota_blocker_not_active_connection

    @StringRes
    @JvmField
    val OTA_BLOCKER_NOT_CHARGING = R.string.ota_blocker_not_charging

    @StringRes
    @JvmField
    val OTA_BLOCKER_NOT_ENOUGH_BATTERY = R.string.ota_blocker_not_enough_battery

    @StringRes
    @JvmField
    val OTA_BLOCKER_NO_INTERNET = R.string.ota_blocker_no_internet
}
