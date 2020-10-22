/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import com.kolibree.android.homeui.hum.databinding.ViewFrequencyChartBinding
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.LocalDate

internal interface FrequencyChartInteraction {
    fun onDayClick(day: LocalDate)
}

internal class FrequencyChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewFrequencyChartBinding

    private var interaction: FrequencyChartInteraction? = null

    init {
        val inflater = LayoutInflater.from(context)
        binding = ViewFrequencyChartBinding.inflate(inflater, this, true)
        binding.viewState = FrequencyChartViewState()
        binding.executePendingBindings()
    }

    fun refresh(newViewState: FrequencyChartViewState) {
        binding.viewState = newViewState
        binding.executePendingBindings()
    }

    fun setInteraction(interaction: FrequencyChartInteraction) {
        binding.interaction = interaction
        binding.executePendingBindings()
    }
}

@Parcelize
internal data class FrequencyChartViewState(
    val days: List<DayType> = emptyList()
) : Parcelable {
    fun weekAt(weekPosition: Int): WeekDaysType {
        val days = mutableListOf<DayType>()
        val firstDayPosition = weekPosition * DAYS_IN_WEEK
        for (day in 0 until DAYS_IN_WEEK) {
            days += dayAt(firstDayPosition + day)
        }
        return WeekDaysType(days)
    }

    private fun dayAt(dayPosition: Int): DayType {
        return days.getOrNull(dayPosition) ?: DayType.EmptyDay
    }
}

@BindingAdapter("frequencyViewState")
internal fun FrequencyChartView.bindViewState(viewState: FrequencyChartViewState?) {
    if (viewState != null) {
        refresh(viewState)
    }
}

@BindingAdapter("frequencyInteraction")
internal fun FrequencyChartView.bindInteraction(interaction: FrequencyChartInteraction?) {
    if (interaction != null) {
        setInteraction(interaction)
    }
}

private const val DAYS_IN_WEEK = 7
