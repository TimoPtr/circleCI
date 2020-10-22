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
import com.kolibree.android.clock.TrustedClock
import org.threeten.bp.LocalDate
import org.threeten.bp.Month

@Keep
class KolibreeBirthDatePickerDialog(context: Context, val initialValue: LocalDate?) :
    KolibreeDualPicker<LocalDate?>(context, initialValue) {

    private val years = buildYears(earliestYear(), latestYear())
    private val months = buildMonths()

    override fun doBuild(parent: ConstraintLayout, constraintSet: ConstraintSet): View {
        val lastView = super.doBuild(parent, constraintSet)
        val month = initialValue?.month ?: Month.JANUARY
        val year = initialValue?.year ?: DEFAULT_YEAR
        val pickerMajor: NumberPicker = parent.setupMajorPicker(month)
        val pickerMinor: NumberPicker = parent.setupMinorPicker(year)
        updateValue(LocalDate.of(pickerMinor.value, pickerMajor.value, 1))
        pickerMajor.setOnValueChangedListener { _, _, newVal ->
            updateValue(LocalDate.of(pickerMinor.value, newVal, 1))
        }
        pickerMinor.setOnValueChangedListener { _, _, newVal ->
            updateValue(LocalDate.of(newVal, pickerMajor.value, 1))
        }
        return lastView
    }

    private fun ConstraintLayout.setupMajorPicker(month: Month): NumberPicker =
        safeFind<NumberPicker>(R.id.picker_major).apply {
            wrapSelectorWheel = true
            displayedValues = months
            minValue = 1
            maxValue = months.size
            value = month.value
        }

    private fun ConstraintLayout.setupMinorPicker(year: Int): NumberPicker =
        safeFind<NumberPicker>(R.id.picker_minor).apply {
            wrapSelectorWheel = false
            displayedValues = years
            minValue = earliestYear()
            maxValue = latestYear()
            value = year
        }

    companion object {
        private const val DEFAULT_YEAR = 2000
        private const val EARLIEST_YEAR = 1900
        fun earliestYear() = EARLIEST_YEAR
        fun latestYear() = TrustedClock.getNowLocalDate().year - 1
    }
}
