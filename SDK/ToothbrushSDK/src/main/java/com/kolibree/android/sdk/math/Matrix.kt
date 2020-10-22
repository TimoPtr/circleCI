package com.kolibree.android.sdk.math

import androidx.annotation.Keep

/**
 * Created by aurelien on 21/07/17.
 *
 * 3*3 matrix class used for raw data manipulation
 */

@Keep
internal class Matrix {
    private companion object {
        const val ROWS = 3
        const val COLUMNS = 3
    }

    private val data: Array<FloatArray> = Array(ROWS) { FloatArray(COLUMNS) }

    /**
     * Get value at specific coordinates
     *
     * @param x x axis index
     * @param y y axis index
     * @return float value
     */
    operator fun get(x: Int, y: Int): Float {
        return data[x][y]
    }

    /**
     * Set the value at specific coordinates
     *
     * @param x x axis index
     * @param y y axis index
     * @param value float value
     */
    operator fun set(x: Int, y: Int, value: Float) {
        data[x][y] = value
    }
}
