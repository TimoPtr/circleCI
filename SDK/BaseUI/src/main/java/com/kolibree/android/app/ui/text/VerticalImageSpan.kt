/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes

/**
 * ALIGN_CENTER was added in Api 29 to DynamicDrawableSpan. This ImageSpan descendant copies the
 * content of draw that are not yet deployed on versions < 29
 *
 * @see https://stackoverflow.com/a/38788432/218473
 */
internal class VerticalImageSpan(
    context: Context,
    @DrawableRes drawableResId: Int
) : ImageSpan(context, drawableResId) {

    /**
     * see detail message in android.text.TextLine
     *
     * @param canvas the canvas, can be null if not rendering
     * @param text the text to be draw
     * @param start the text start position
     * @param end the text end position
     * @param x the edge of the replacement closest to the leading margin
     * @param top the top of the line
     * @param y the baseline
     * @param bottom the bottom of the line
     * @param paint the work paint
     */
    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.save()

        val transY: Int = top + (bottom - top) / 2 - drawable.bounds.height() / 2

        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}
