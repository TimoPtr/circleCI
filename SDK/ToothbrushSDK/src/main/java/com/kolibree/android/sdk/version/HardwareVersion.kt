package com.kolibree.android.sdk.version

import androidx.annotation.Keep
import java.util.Locale

/**
 * Created by aurelien on 28/07/17.
 *
 *
 * Hardware version model
 *
 * Default constructor
 *
 * @param major int major
 * @param minor int minor
 */
@Keep
class HardwareVersion(major: Int, minor: Int) : BaseVersion(major.toLong(), minor.toLong()) {

    /**
     * Binary constructor
     *
     * @param binary long-encapsulated software version example : 0x000A0001 for 10.1
     */
    constructor(binary: Long) : this((binary shr 16).toInt(), (binary and 0xFFFF).toInt())

    /**
     * Parser constructor
     *
     *
     * Accepts strings following the format MAJOR.MINOR
     *
     *
     * For example
     *
     *
     * - "2.4" - "0.1"
     *
     *
     * Any other format will throw an IndexOutOfBoundsException
     *
     * @param versionString String readable version
     */
    constructor(versionString: String) : this(
        getVersionComponent(versionString, MAJOR_INDEX).toInt(),
        getVersionComponent(versionString, MINOR_INDEX).toInt()
    )

    override fun toString(): String =
        String.format(Locale.US, "%d.%d", value[MAJOR_INDEX], value[MINOR_INDEX])

    override fun toBinary(): Long =
        value[MAJOR_INDEX] shl 16 or (value[MINOR_INDEX] and 0xFFFFL)

    companion object {
        // null object pattern https://en.wikipedia.org/wiki/Null_object_pattern
        @JvmField
        val NULL = HardwareVersion(0, 0)
    }
}
