/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.profile

import androidx.annotation.Keep

@Keep
enum class SourceApplication(
    val serializedName: String
) {
    COLGATE_CONNECT(SOURCE_APPLICATION_COLGATE_CONNECT),
    DATAPP(SOURCE_APPLICATION_DATAPP),
    HUM(SOURCE_APPLICATION_HUM),
    KOLIBREE(SOURCE_APPLICATION_KOLIBREE),
    LEGACY(SOURCE_APPLICATION_LEGACY),
    MAGIK(SOURCE_APPLICATION_MAGIK),
    UNKNOWN(SOURCE_APPLICATION_UNKNOWN);

    companion object {

        @JvmStatic
        fun findBySerializedName(serializedName: String?) =
            values()
                .firstOrNull { it.serializedName == serializedName }
                ?: UNKNOWN
    }
}

const val SOURCE_APPLICATION_COLGATE_CONNECT = "colgateconnect"
const val SOURCE_APPLICATION_DATAPP = "datapp"
const val SOURCE_APPLICATION_HUM = "hum"
const val SOURCE_APPLICATION_KOLIBREE = "kolibree"
const val SOURCE_APPLICATION_LEGACY = "legacy"
const val SOURCE_APPLICATION_MAGIK = "magik"
const val SOURCE_APPLICATION_UNKNOWN = ""
