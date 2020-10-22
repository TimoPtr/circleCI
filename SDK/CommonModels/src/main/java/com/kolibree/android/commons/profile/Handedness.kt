/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.android.commons.profile

/** Kolibree handedness definition  */
enum class Handedness(
    val serializedName: String
) {
    RIGHT_HANDED(HANDEDNESS_RIGHT),
    LEFT_HANDED(HANDEDNESS_LEFT),
    UNKNOWN(HANDEDNESS_UNKNOWN);

    companion object {

        @JvmStatic
        fun findBySerializedName(serializedName: String?): Handedness =
            values()
                .firstOrNull { it.serializedName == serializedName }
                ?: UNKNOWN
    }
}

const val HANDEDNESS_LEFT = "L"
const val HANDEDNESS_RIGHT = "R"
const val HANDEDNESS_UNKNOWN = ""
