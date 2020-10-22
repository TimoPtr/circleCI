/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.binding

import android.content.res.Resources
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.databinding.BindingAdapter
import com.kolibree.android.homeui.hum.R
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

@BindingAdapter("headerValueItem")
internal fun TextView.setValueOrRes(item: HeaderValueSettingsItemBindingModel) {
    text = if (item.valueRes != UNDEFINED_RESOURCE_ID) {
        context.getText(item.valueRes)
    } else {
        item.value
    }
}

@BindingAdapter("headerFormattedValueItem")
internal fun TextView.setFormattedValue(item: HeaderFormattedValueSettingsItemBindingModel<*>) {
    text = item.formattedValue(context)
}

internal fun durationFormatter(resources: Resources, value: Duration): String {
    val minutes = value.toMinutes().toInt()
    val seconds = value.minusMinutes(minutes.toLong()).seconds.toInt()
    return StringBuilder().apply {
        if (minutes != 0) {
            append(resources.formatString(R.plurals.settings_brushing_time_format_minutes, minutes))
        }
        if (seconds != 0) {
            if (isNotEmpty()) append(" ")
            append(resources.formatString(R.plurals.settings_brushing_time_format_seconds, seconds))
        }
    }.toString()
}

private fun Resources.formatString(@PluralsRes resId: Int, value: Int) =
    getQuantityString(resId, value, value)

private const val BIRTH_DATE_FORMAT = "MMM yyyy"
private val birthDateFormatter = DateTimeFormatter.ofPattern(BIRTH_DATE_FORMAT)

internal fun birthDateFormatter(resources: Resources, value: LocalDate?): String =
    value?.let {
        birthDateFormatter.format(value)
    } ?: resources.getString(R.string.unknown)
