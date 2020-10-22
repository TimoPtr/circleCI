/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.shape

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.shape.EdgeTreatment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapePath
import com.kolibree.R
import com.kolibree.android.extensions.getColorFromAttr

class ShapeSpikeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shape_spike)

        findViewById<ImageView>(R.id.image).apply {
            val radius = resources.getDimension(R.dimen.dot_six)
            background = MaterialShapeDrawable(
                ShapeAppearanceModel.builder()
                    .setBottomRightCornerSize(radius)
                    .setBottomRightCorner(RoundedCornerTreatment())
                    .setBottomEdge(ColgateBottomEdge(radius))
                    .build()
            ).apply {
                /*
                 * It should be possible to introspect the existing background to extract the
                 * colour. I have just hard-coded this to show the shape concepts.
                 */
                val color = context.getColorFromAttr(R.attr.colorPrimary)
                fillColor = ColorStateList.valueOf(color)
            }
        }
    }

    /*
     * Even though this is doing something funky with the corner, it actually needs to be an
     * EdgeTreatment rather than a CornerTreatment. The reason for this is CornerTreatment can
     * only draw within the view bounds, whereas EdgeTreatment can draw outside it. However, the
     * parent layout *must* declare `android:clipChildren="false"` otherwise the edges will be
     * clipped to the view bounds as well.
     */
    private class ColgateBottomEdge(private val radius: Float) : EdgeTreatment() {
        override fun getEdgePath(
            length: Float,
            center: Float,
            interpolation: Float,
            shapePath: ShapePath
        ) {
            val interpolatedRadius = radius * interpolation
            shapePath.lineTo(length - interpolatedRadius, 0f)
            shapePath.addArc(
                length - (2 * interpolatedRadius),
                -interpolatedRadius * 2,
                length,
                0f,
                NINETY_DEGREES,
                -NINETY_DEGREES
            )
            shapePath.lineTo(length, 0f)
        }
    }

    companion object {
        private const val NINETY_DEGREES = 90f
    }
}
