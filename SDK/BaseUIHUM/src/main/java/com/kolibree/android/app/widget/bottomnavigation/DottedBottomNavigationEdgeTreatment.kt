/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget.bottomnavigation

import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.ShapePath

internal class DottedBottomNavigationEdgeTreatment(
    private val dotRadius: Float,
    dotPadding: Float
) : EdgeTreatment() {

    var horizontalOffset: Float = 0f

    private val bulbDiameter = (dotPadding * 2f) + (dotRadius * 2f)

    override fun getEdgePath(length: Float, center: Float, interpolation: Float, shapePath: ShapePath) {
        if (dotRadius == 0f) {
            shapePath.lineTo(length, 0f)
        } else {
            shapePath.buildBulb(length, interpolation)
        }
    }

    private fun ShapePath.buildBulb(length: Float, interpolation: Float) {
        val innerLeftPosition = (horizontalOffset - bulbDiameter) * interpolation
        val outerLeftPosition = innerLeftPosition - (bulbDiameter * interpolation)
        val innerRightPosition = (horizontalOffset + bulbDiameter) * interpolation
        val outerRightPosition = innerRightPosition + (bulbDiameter * interpolation)
        lineTo(outerLeftPosition, 0f)
        addArc(outerLeftPosition, -bulbDiameter, innerLeftPosition, 0f, LEFT_START, SMALL_SWEEP)
        addArc(innerLeftPosition, -bulbDiameter, innerRightPosition, bulbDiameter, LARGE_START, LARGE_SWEEP)
        addArc(innerRightPosition, -bulbDiameter, outerRightPosition, 0f, RIGHT_START, SMALL_SWEEP)
        lineTo(length, 0f)
    }

    private companion object {
        private const val LARGE_START = -135f
        private const val LARGE_SWEEP = 90f
        private const val SMALL_SWEEP = -45f
        private const val LEFT_START = 90f
        private const val RIGHT_START = 135f
    }
}
