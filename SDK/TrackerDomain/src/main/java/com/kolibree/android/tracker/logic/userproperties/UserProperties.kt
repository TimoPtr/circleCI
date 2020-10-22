/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic.userproperties

import androidx.annotation.Keep

@Keep
object UserProperties {

    const val USER_TYPE = "beta"

    const val BETA_USER = "1"

    const val REGULAR_USER = "0"

    const val ACCOUNT = "accountBackendId"

    const val PROFILE = "profileBackendId"

    const val PUB_ID = "pubId"

    const val FIRST_NAME = "firstName"

    const val GENDER = "gender"

    const val GENDER_MALE = "M"

    const val GENDER_FEMALE = "F"

    const val GENDER_UNSET = "U"

    const val COUNTRY = "country"

    const val HANDEDNESS = "handedness"

    const val HANDEDNESS_RIGHT = "R"

    const val HANDEDNESS_LEFT = "L"

    const val HANDEDNESS_UNKNOWN = "U"

    const val HARDWARE_VERSION = "hardwareVersion"

    const val FIRMWARE_VERSION = "firmwareVersion"

    const val MAC_ADDRESS = "macAddress"

    const val SERIAL_NUMBER = "serialNumber"

    const val TOOTHBRUSH_MODEL = "model"

    const val STUDY_NAME = "study_name"

    const val LOCALE = "locale"

    const val FIRST_DAY_OF_WEEK = "firstDayOfWeek"

    val ALL_PROPERTIES = setOf(
        USER_TYPE,
        ACCOUNT,
        PROFILE,
        PUB_ID,
        FIRST_NAME,
        GENDER,
        COUNTRY,
        HANDEDNESS,
        HARDWARE_VERSION,
        FIRMWARE_VERSION,
        MAC_ADDRESS,
        SERIAL_NUMBER,
        TOOTHBRUSH_MODEL,
        STUDY_NAME,
        LOCALE,
        FIRST_DAY_OF_WEEK
    )
}
