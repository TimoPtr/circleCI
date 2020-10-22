/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.commons.profile

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting

/** Kolibree gender definition  */
@Keep
enum class Gender(
    val serializedName: String
) {
    MALE(GENDER_MALE_SERIALIZED_VALUE),
    FEMALE(GENDER_FEMALE_SERIALIZED_VALUE),
    PREFER_NOT_TO_ANSWER(GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE),
    UNKNOWN(GENDER_UNKNOWN_SERIALIZED_VALUE);

    companion object {

        @JvmStatic
        fun findBySerializedName(serializedName: String?) =
            values()
                .firstOrNull { it.serializedName == serializedName }
                ?: UNKNOWN
    }
}

@VisibleForTesting
internal const val GENDER_MALE_SERIALIZED_VALUE = "M"

@VisibleForTesting
internal const val GENDER_FEMALE_SERIALIZED_VALUE = "F"

@VisibleForTesting
internal const val GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE = "NC"

@VisibleForTesting
internal const val GENDER_UNKNOWN_SERIALIZED_VALUE = "U"
