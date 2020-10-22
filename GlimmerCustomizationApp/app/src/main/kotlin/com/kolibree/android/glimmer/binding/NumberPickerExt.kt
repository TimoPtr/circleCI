/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.binding

import android.view.View
import android.widget.Button
import androidx.databinding.BindingAdapter
import com.kolibree.android.glimmer.utils.NumberPicker
import com.kolibree.android.sdk.connection.brushingmode.customizer.curve.BrushingModeCurveSettings

@BindingAdapter("value")
internal fun NumberPicker.bindValue(value: Int) {
    this.value = value
}

@BindingAdapter("min")
internal fun NumberPicker.min(value: Int) {
    min = value
}

@BindingAdapter("max")
internal fun NumberPicker.max(value: Int) {
    max = value
}

@BindingAdapter("enabled")
internal fun NumberPicker.bindEnabled(enabled: Boolean) {
    val visibility = if (enabled) View.VISIBLE else View.INVISIBLE
    findViewById<Button>(com.travijuu.numberpicker.library.R.id.decrement).visibility = visibility
    findViewById<Button>(com.travijuu.numberpicker.library.R.id.increment).visibility = visibility
    setDisplayFocusable(enabled)
}

fun NumberPicker.setup(maxValue: Int, valueListener: (Int) -> Unit) {
    max = maxValue
    min = 0
    setValueChangedListener { value, _ -> valueListener(value) }
}

fun NumberPicker.setup(valueListener: (Int) -> Unit) {
    setValueChangedListener { value, _ -> valueListener(value) }
}

fun NumberPicker.setupAsSlope(valueListener: (Int) -> Unit) = setup(
    maxValue = BrushingModeCurveSettings.MAX_SLOPE,
    valueListener = valueListener
)
