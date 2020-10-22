package com.kolibree.android.sdk.version

import androidx.annotation.Keep
import java.util.Locale

/**
 * Created by aurelien on 28/07/17.
 *
 *
 * Software version model
 *
 * Default constructor
 *
 * @param major int major
 * @param minor int minor
 * @param revision int revision
 */
@Keep
class SoftwareVersion(
    major: Int,
    minor: Int,
    @JvmField
    val revision: Int
) : BaseVersion(major.toLong(), minor.toLong(), revision.toLong()) {

    /**
     * Parser constructor
     *
     *
     * Accepts strings following the format MAJOR.MINOR.REVISION
     *
     *
     * For example
     *
     *
     * - "12.3.5436" - "0.1.6"
     *
     *
     * Any other format will throw an IndexOutOfBoundsException
     *
     * @param versionString String readable version
     */
    constructor(versionString: String) : this(
        getVersionComponent(versionString, MAJOR_INDEX).toInt(),
        getVersionComponent(versionString, MINOR_INDEX).toInt(),
        getVersionComponent(versionString, REVISION_INDEX).toInt()
    )

    /**
     * Binary constructor
     *
     * @param binary long-encapsulated software version example : 0x000A0001 for 0.10.1
     */
    constructor(binary: Long) : this(
        ((binary shr 24) and 0xFF).toInt(),
        ((binary shr 16) and 0xFF).toInt(),
        (binary and 0xFFFF).toInt()
    )

    override fun toString(): String =
        String.format(
            Locale.US,
            "%d.%d.%d",
            value[MAJOR_INDEX],
            value[MINOR_INDEX],
            value[REVISION_INDEX]
        )

    override fun toBinary(): Long =
        value[MAJOR_INDEX] shl 24 or (value[MINOR_INDEX] shl 16) or (value[REVISION_INDEX] and 0x0000FFFFL)

    fun isNull(): Boolean = this == NULL

    companion object {

        // null object pattern https://en.wikipedia.org/wiki/Null_object_pattern
        @JvmField
        val NULL = SoftwareVersion(0, 0, 0)

        const val REVISION_INDEX = 2
    }
}
