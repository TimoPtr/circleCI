package com.kolibree.android.sdk.version

import java.security.InvalidParameterException

/**
 * Created by aurelien on 28/07/17.
 *
 *
 * BaseVersion utility
 *
 *
 * Immutable
 */
abstract class BaseVersion internal constructor(vararg value: Long) : Comparable<BaseVersion> {

    protected val value: LongArray

    init {
        if (!acceptableVersionSize.contains(value.size)) {
            throw InvalidParameterException("Only M.m.r and M.m version formats are supported")
        }

        this.value = value
    }

    /**
     * The major digit of the version
     */
    @JvmField
    val major: Int = value[0].toInt()

    /**
     * The minor digit of the version
     */
    @JvmField
    val minor: Int = value[1].toInt()

    override fun equals(other: Any?): Boolean = other is BaseVersion && value.contentEquals(other.value)

    override fun hashCode(): Int = value.contentHashCode()

    override fun compareTo(other: BaseVersion): Int {
        require(value.size == other.value.size) { "Software versions can't be compared to hardware ones" }

        for (i in value.indices) {
            if (value[i] != other.value[i]) {
                return (value[i] - other.value[i]).toInt()
            }
        }

        return 0
    }

    fun isNewer(another: BaseVersion): Boolean = compareTo(another) > 0

    fun isNewerOrSame(another: BaseVersion): Boolean = compareTo(another) >= 0

    abstract override fun toString(): String

    /**
     * Get a long-wrapped binary representation of the version
     *
     * @return long binary representation
     */
    abstract fun toBinary(): Long

    internal companion object {

        /**
         * Extract a version component from a string
         *
         * @param version non null String
         * @param index int index (0 = major)
         * @return Long
         */
        @JvmStatic
        fun getVersionComponent(version: String, index: Int): Long =
            version.split(".")[index].toLong()

        private val acceptableVersionSize = listOf(2, 3)

        const val MAJOR_INDEX = 0
        const val MINOR_INDEX = 1
    }
}
