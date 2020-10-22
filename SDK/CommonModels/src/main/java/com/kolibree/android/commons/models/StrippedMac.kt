/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.models

import com.kolibree.android.annotation.VisibleForApp

/**
 * String that guarantees
 * - Common MAC separators are removed. colon (:), hyphen (-) or blank space will be removed
 * - Does not contain new lines
 * - Does not contain blank spaces at start ot end
 *
 * This class does not validate that it's a well formed MAC Address
 */
@VisibleForApp
class StrippedMac private constructor(val value: String) {
    @VisibleForApp
    companion object {
        @JvmStatic
        fun fromMac(mac: String) = StrippedMac(
            mac.trim().replace(" ", "").replace("\n", "").replace(":", "").replace("-", "")
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StrippedMac

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}
