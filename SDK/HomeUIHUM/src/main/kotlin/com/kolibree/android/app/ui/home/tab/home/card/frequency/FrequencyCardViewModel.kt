/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.frequency

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDot
import com.kolibree.android.app.ui.home.pulsingdot.domain.PulsingDotUseCase
import com.kolibree.android.app.ui.home.tab.view.WeekDayLabels
import com.kolibree.android.app.ui.home.toolbox.ToolboxViewModel
import com.kolibree.android.extensions.atStartOfKolibreeDay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.Locale
import javax.inject.Inject
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import timber.log.Timber

internal class FrequencyCardViewModel(
    initialViewState: FrequencyCardViewState,
    private val frequencyBrushingUseCase: FrequencyBrushingUseCase,
    private val homeNavigator: HumHomeNavigator,
    private val pulsingDotUseCase: PulsingDotUseCase,
    private val toolboxViewModel: ToolboxViewModel
) : DynamicCardViewModel<
    FrequencyCardViewState,
    FrequencyCardInteraction,
    FrequencyCardBindingModel>(initialViewState), FrequencyCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is FrequencyCardBindingModel

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::retrievePulsingDotStatus)
    }

    override fun onPageBackClick() {
        FrequencyChartAnalytics.previousMonth()
        updateViewState { copy(currentMonthFromNow = previousMonth()) }
        prepareDataBeforeCurrentMonth()
    }

    override fun onPageForwardClick() {
        FrequencyChartAnalytics.nextMonth()
        updateViewState { copy(currentMonthFromNow = nextMonth()) }
        prepareDataBeforeCurrentMonth()
    }

    private fun prepareDataBeforeCurrentMonth() {
        getViewState()?.let {
            val currentMonth = it.month()
            prepareDataBeforeMonth(currentMonth)
        }
    }

    private fun prepareDataBeforeMonth(month: YearMonth) {
        disposeOnCleared {
            frequencyBrushingUseCase.prepareDataBeforeMonth(month)
                .subscribe({}, Timber::e)
        }
    }

    override fun onDayClick(day: LocalDate) {
        FrequencyChartAnalytics.day()
        homeNavigator.navigateToDayCheckup(day.atStartOfKolibreeDay())
    }

    override fun onPulsingDotClick() {
        pulsingDotUseCase.onPulsingDotClicked(PulsingDot.FREQUENCY_CHART)
        toolboxViewModel.show(toolboxViewModel.factory().frequencyChart())
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        observeFrequencyChartData()
        updateWeekLabels()
    }

    private fun updateWeekLabels() {
        updateViewState {
            copy(weekDayLabels = WeekDayLabels.create(Locale.getDefault()))
        }
    }

    private fun observeFrequencyChartData() {
        disposeOnStop {
            frequencyBrushingUseCase.getBrushingStateForCurrentProfile()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::refreshMonthData, Timber::e)
        }
    }

    private fun refreshMonthData(data: Pair<Profile, List<FrequencyChartViewState>>) {
        val (newProfile, newMonthsData) = data
        updateViewState {
            copy(
                profileId = newProfile.id,
                monthsData = newMonthsData,
                currentMonthFromNow = if (profileId == newProfile.id) currentMonthFromNow else 0
            )
        }
    }

    private fun retrievePulsingDotStatus(): Disposable? {
        return pulsingDotUseCase.shouldShowPulsingDot(PulsingDot.FREQUENCY_CHART)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onPulsingDotStatusRetrieved, Timber::e)
    }

    private fun onPulsingDotStatusRetrieved(shouldShow: Boolean) {
        updateViewState { copy(pulsingDotVisible = shouldShow) }
    }

    class Factory @Inject constructor(
        private val frequencyBrushingUseCase: FrequencyBrushingUseCase,
        private val homeNavigator: HumHomeNavigator,
        private val pulsingDotUseCase: PulsingDotUseCase,
        private val toolboxViewModel: ToolboxViewModel,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<FrequencyCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FrequencyCardViewModel(
            viewState ?: FrequencyCardViewState.initial(
                dynamicCardListConfiguration.getInitialCardPosition(modelClass)
            ),
            frequencyBrushingUseCase,
            homeNavigator,
            pulsingDotUseCase,
            toolboxViewModel
        ) as T
    }
}
