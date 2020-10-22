/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.version

import androidx.annotation.Keep
import java.util.Locale

/**
 * Default constructor
 *
 * @param major int major
 * @param minor int minor
 * @param algorithm int algorithm
 */
@Keep
@Suppress("MagicNumber")
class DspVersion(
    major: Int,
    minor: Int,
    val algorithm: Long
) : BaseVersion(major.toLong(), minor.toLong(), algorithm) {
    /**
     * Binary constructor
     *
     * @param binary long-encapsulated software version example : 0x000A0001 for 0.10.1
     */
    constructor(binary: Long) : this(
        major = ((binary shr 24) and 0xFF).toInt(),
        minor = ((binary shr 16) and 0xFF).toInt(),
        algorithm = binary and 0x0000FFFFL
    )

    override fun toBinary(): Long =
        value[MAJOR_INDEX] shl 24 or (value[MINOR_INDEX] shl 16) or (value[ALGORITHM_INDEX] and 0x0000FFFFL)

    override fun toString(): String =
        String.format(
            Locale.US,
            "%d.%d.%d",
            value[MAJOR_INDEX],
            value[MINOR_INDEX],
            value[ALGORITHM_INDEX]
        )

    companion object {

        // null object pattern https://en.wikipedia.org/wiki/Null_object_pattern
        @JvmField
        val NULL = DspVersion(0, 0, 0)

        private const val ALGORITHM_INDEX = 2
    }
}
