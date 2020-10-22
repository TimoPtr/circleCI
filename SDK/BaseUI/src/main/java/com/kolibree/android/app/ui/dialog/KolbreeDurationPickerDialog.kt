/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.dialog

import android.content.Context
import android.view.View
import android.widget.NumberPicker
import androidx.annotation.Keep
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.kolibree.android.baseui.R
import org.threeten.bp.Duration

@Keep
class KolbreeDurationPickerDialog(context: Context, val initialValue: Duration) :
    KolibreeDualPicker<Duration>(context, initialValue) {

    private var durations: List<Duration> = emptyList()
    private var minuteValues: List<Int> = emptyList()
    private var minuteStrings = emptyArray<String>()
    private var secondsStrings = emptyArray<String>()
    private var increment: Duration = Duration.ofSeconds(1)

    /**
     * Sets the range for the duration dialog.
     * @param start The lower bound of the duration selection
     * @param end The upper bound of the duration selection
     * @param increment The interval between values
     */
    fun setRange(start: Duration, end: Duration, increment: Duration) {
        this.increment = increment
        durations = buildValuesRange(start, end, increment)
        minuteValues = buildMinuteValues(durations)
        minuteStrings = buildMinuteStrings(minuteValues)
        secondsStrings = buildSecondsStrings(durations)
    }

    override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
        val lastView = super.doBuild(parent, constraintSet)
        val secondsIndex = durations.indexOf(initialValue)
        val minuteIndex = minuteValues.indexOf(durations[secondsIndex].toMinutes().toInt())
        val pickerMajor: NumberPicker = parent.setupMajorPicker(minuteIndex)
        val pickerMinor: NumberPicker = parent.setupMinorPicker(secondsIndex)
        pickerMajor.setOnValueChangedListener(MajorValueChangeListener(pickerMinor))
        pickerMinor.setOnValueChangedListener(MinorValueChangeListener(pickerMajor))
        return lastView
    }

    private fun ConstraintLayout.setupMajorPicker(index: Int): NumberPicker =
        safeFind<NumberPicker>(R.id.picker_major).apply {
            wrapSelectorWheel = false
            displayedValues = minuteStrings
            minValue = 0
            maxValue = minuteStrings.size - 1
            value = index
        }

    private fun ConstraintLayout.setupMinorPicker(index: Int): NumberPicker =
        safeFind<NumberPicker>(R.id.picker_minor).apply {
            wrapSelectorWheel = false
            minValue = 0
            displayedValues = secondsStrings
            maxValue = secondsStrings.size - 1
            value = index
        }

    private inner class MajorValueChangeListener(private val pickerMinor: NumberPicker) :
        NumberPicker.OnValueChangeListener {

        override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
            val oldIndex = pickerMinor.value
            val oldValue = durations[oldIndex]
            val newIndex = if (oldVal < newVal) {
                val newValue = oldValue.plusMinutes(1)
                durations.indexOf(newValue).let { newIndex ->
                    if (newIndex == -1) durations.size - 1 else newIndex
                }
            } else {
                val newValue = oldValue.minusMinutes(1)
                durations.indexOf(newValue).let { newIndex ->
                    if (newIndex == -1) 0 else newIndex
                }
            }
            pickerMinor.value = newIndex
            updateValue(durations[newIndex])
        }
    }

    private inner class MinorValueChangeListener(private val pickerMajor: NumberPicker) :
        NumberPicker.OnValueChangeListener {

        override fun onValueChange(picker: NumberPicker, oldVal: Int, newVal: Int) {
            pickerMajor.value = minuteValues.indexOf(durations[newVal].toMinutes().toInt())
            updateValue(durations[newVal])
        }
    }
}
