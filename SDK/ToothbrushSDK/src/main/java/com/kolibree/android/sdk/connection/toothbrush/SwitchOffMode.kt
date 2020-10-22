/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush

import androidx.annotation.Keep

/**
 * Toothbrush switch-off modes definition
 */

@Keep
enum class SwitchOffMode {

    /**
     * Power down the toothbrush
     */
    HARD_OFF,

    /**
     * Reboot the toothbrush
     */
    REBOOT,

    /**
     * Put the toothbrush in sleep mode
     */
    SOFT_OFF,

    /**
     * CAUTION: don't use if you don't know why you would not prefer HARD_OFF
     *
     * This will put the toothbrush in deep sleep mode like it is when it goes out of the factory
     */
    FACTORY_HARD_OFF,

    /**
     * CAUTION: don't use if you don't really know what you are doing
     *
     * This will reset the toothbrush's RTC module
     */
    RESET_BACKUP_DOMAIN,

    /**
     * CAUTION: this will have effects only in Glint
     *
     * This is similar with hard off, but contrarily to hard off it does not reset RTC so offline
     * brushings can be stored directly after startup
     */
    TRAVEL_MODE
}
