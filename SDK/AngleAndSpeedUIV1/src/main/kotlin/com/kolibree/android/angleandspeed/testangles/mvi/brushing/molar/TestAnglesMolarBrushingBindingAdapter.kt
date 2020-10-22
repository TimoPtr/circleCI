/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.brushing.molar

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.angleandspeed.R
import com.kolibree.android.angleandspeed.testangles.model.ToothSide

internal const val UPDATE_DELAY_MS = 1000L

@SuppressLint("BinaryOperationInTimber")
@Keep
@BindingAdapter(
    value = ["isZoneCorrect", "brushDegree", "brushSide", "brushRadian", "molarAnimationArea"],
    requireAll = true
)
fun ImageView.bindBrushHead(
    isZoneCorrect: Boolean?,
    brushDegree: Float?,
    brushSide: ToothSide?,
    brushRadian: Double?,
    molarAnimationArea: Rect?
) {
    clearAnimation()
    if (brushDegree == null || brushSide == null || molarAnimationArea == null) return

    MolarAnimation(this, molarAnimationArea).start(brushDegree, brushSide)

    updateHeadState(this, isZoneCorrect, brushRadian)
}

private fun updateHeadState(
    view: ImageView,
    isZoneCorrect: Boolean?,
    brushRadian: Double?
) {
    if (isZoneCorrect != null && brushRadian != null) {
        view.handler.postDelayed({
            view.visibility = View.VISIBLE
            view.setImageResource(
                when {
                    isZoneCorrect -> R.drawable.ic_brush_front_green
                    brushRadian in -NEUTRAL_DEGREE_BORDER_VALUE..NEUTRAL_DEGREE_BORDER_VALUE ->
                        R.drawable.ic_brush_front_blue
                    else -> R.drawable.ic_brush_front_red
                }
            )
        }, UPDATE_DELAY_MS)
    }
}

@Keep
@BindingAdapter(value = ["stateColor", "brushDegree"], requireAll = true)
fun TextView.bindDegreeText(stateColor: Int?, brushDegree: Float?) {
    handler.postDelayed({
        brushDegree?.let {
            visibility = View.VISIBLE
            text = context.getString(R.string.test_angles_degrees_format).format(it.toInt())
        }
        stateColor?.let { setTextColor(ContextCompat.getColor(context, it)) }
    }, UPDATE_DELAY_MS)
}

@Keep
@BindingAdapter(value = ["showRightCone"])
fun ImageView.bindRightCone(brushSide: ToothSide?) {
    handler.postDelayed({
        brushSide?.let {
            this.visibility = if (brushSide == ToothSide.RIGHT) View.VISIBLE else View.INVISIBLE
        }
    }, UPDATE_DELAY_MS)
}

@Keep
@BindingAdapter(value = ["showLeftCone"])
fun ImageView.bindLeftCone(brushSide: ToothSide?) {
    handler.postDelayed({
        brushSide?.let {
            this.visibility = if (brushSide == ToothSide.LEFT) View.VISIBLE else View.INVISIBLE
        }
    }, UPDATE_DELAY_MS)
}
