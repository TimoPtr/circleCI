/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.kolibree.android.app.ui.extention.dimenFloat
import com.kolibree.android.extensions.getColorFromAttr
import com.kolibree.android.guidedbrushing.R

/** Guided Brushing coverage gauge view */
internal class GuidedBrushingGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    @ColorInt
    private val backgroundColor = context.getColorFromAttr(R.attr.colorTertiaryMedium)

    private val borderThickness = context.dimenFloat(R.dimen.dot_half)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.TRANSPARENT)

        // Draw border
        paint.color = Color.WHITE
        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)

        // Draw background
        paint.color = backgroundColor
        canvas.drawCircle(width / 2f, height / 2f, width / 2f - borderThickness, paint)
    }
}
