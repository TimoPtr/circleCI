/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.bindingadapter.setLegendDrawableTintColor
import com.kolibree.android.app.ui.widget.CheckupView.Companion.TOUCH_AREA_ANIMATION_DURATION
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.jaws.base.bindShouldRender
import com.kolibree.android.jaws.base.bindUpdateColorMouthZones
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.jaws.hum.HumJawsView
import com.kolibree.kml.MouthZone16
import kotlin.math.max
import kotlin.math.min

/** Hum Checkup view (jaws and alternate no-data screen) */
@VisibleForApp
class CheckupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    internal val jawsView: HumJawsView

    internal val noDataPlaceHolderCaption: View

    internal val manualPlaceHolderCaption: View

    internal val touchAreaView: View

    init {
        View.inflate(context, R.layout.view_checkup, this)
        jawsView = findViewById(R.id.checkup_mouth_map)
        noDataPlaceHolderCaption = findViewById(R.id.checkup_mouth_map_no_data_caption)
        manualPlaceHolderCaption = findViewById(R.id.checkup_manual_brushing_caption)
        touchAreaView = findViewById(R.id.checkup_touch)
        setup()
    }

    fun setCleanZoneColor(@ColorInt color: Int) = jawsView.setCleanZoneColor(color)

    fun setNeglectedZoneColor(@ColorInt color: Int) = jawsView.setNeglectedZoneColor(color)

    private fun setup() {
        findViewById<TextView>(R.id.checkup_missed_legend)
            .setLegendDrawableTintColor(ContextCompat.getColor(context, R.color.neglectedZoneColor))
        setupTouchArea()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchArea() =
        touchAreaView.setOnTouchListener { _, motionEvent ->
            jawsView.onMotionEvent(motionEvent)
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> touchAreaView.fadeOut()
                MotionEvent.ACTION_OUTSIDE,
                MotionEvent.ACTION_UP -> touchAreaView.fadeIn()
            }
            true
        }

    internal companion object {

        internal const val TOUCH_AREA_ANIMATION_DURATION = 300L
    }
}

@BindingAdapter("shouldRender")
internal fun CheckupView.bindShouldRender(shouldRender: Boolean) =
    jawsView.bindShouldRender(shouldRender)

@BindingAdapter("colorMouthZones")
internal fun CheckupView.bindColorMouthZones(colorMouthZones: ColorMouthZones?) =
    colorMouthZones?.let {
        jawsView.bindUpdateColorMouthZones(it)
    }

@VisibleForApp
@SuppressLint("SdkPublicExtensionMethodWithoutKeep")
@BindingAdapter("updateCheckupData")
fun CheckupView.bindUpdateCheckupData(checkupData: Map<MouthZone16, Float>) {
    jawsView.bindUpdateColorMouthZones(
        ColorMouthZones(zonesColor = checkupData.mapValues {
            jawsView.evaluatedColor(min(1f, max(0f, it.value)))
        })
    )
}

// Selects whether we show the jaws or the fallback no data screen or manual mode
@BindingAdapter(value = ["showData", "manualBrushing"], requireAll = false)
internal fun CheckupView.bindShowData(showData: Boolean, isManualBrushing: Boolean = false) {
    if (isManualBrushing) {
        manualPlaceHolderCaption.visibility = VISIBLE
        noDataPlaceHolderCaption.visibility = INVISIBLE
        touchAreaView.visibility = INVISIBLE
    } else {
        manualPlaceHolderCaption.visibility = INVISIBLE
        noDataPlaceHolderCaption.visibility = if (showData) INVISIBLE else VISIBLE
        touchAreaView.visibility = if (showData) VISIBLE else INVISIBLE
    }
}

@BindingAdapter("checkupData")
internal fun CheckupView.bindCheckupData(checkupData: Map<MouthZone16, Float>) {
    if (checkupData.isEmpty()) {
        jawsView.setColorMouthZones(ColorMouthZones.white())
    } else {
        jawsView.setColorMouthZones(checkupData)
    }
}

@Keep
@BindingAdapter("neglectedZoneColor")
fun CheckupView.bindNeglectedZoneColor(@ColorInt color: Int) = setNeglectedZoneColor(color)

@Keep
@BindingAdapter("cleanZoneColor")
fun CheckupView.bindCleanZoneColor(@ColorInt color: Int) = setCleanZoneColor(color)

private fun View.fadeIn() = animate()
    .alpha(1f)
    .setDuration(TOUCH_AREA_ANIMATION_DURATION)
    .start()

private fun View.fadeOut() = animate()
    .alpha(0f)
    .setDuration(TOUCH_AREA_ANIMATION_DURATION)
    .start()
