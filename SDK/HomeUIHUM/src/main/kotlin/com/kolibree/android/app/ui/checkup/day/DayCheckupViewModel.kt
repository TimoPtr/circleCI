/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.day

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.KOLIBREE_DAY_START_HOUR
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.checkup.base.BaseCheckupViewModel
import com.kolibree.android.app.ui.checkup.base.CheckupActions
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.distinctUntilChanged
import com.kolibree.kml.MouthZone16
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber

/** Checkup [BaseViewModel] implementation */
@VisibleForApp
class DayCheckupViewModel(
    initialViewState: DayCheckupViewState,
    private val currentProfileProvider: CurrentProfileProvider,
    private val brushingFacade: BrushingFacade,
    private val checkupCalculator: CheckupCalculator,
    private val forDate: OffsetDateTime
) : BaseCheckupViewModel<DayCheckupViewState>(
    initialViewState = initialViewState,
    brushingFacade = brushingFacade
) {

    @VisibleForTesting
    internal val sessionCache = AtomicReference<MutableList<DayCheckupData>>(mutableListOf())

    @VisibleForTesting
    var currentPosition: Int = 0

    val checkupDataListLiveData: LiveData<List<Map<MouthZone16, Float>>> = mapNonNull(
        viewStateLiveData,
        defaultValue = initialViewState.checkupData
    ) { viewState -> viewState.checkupData }.distinctUntilChanged()

    val pagerIndicatorVisibleLiveData: LiveData<Boolean> = mapNonNull(
        viewStateLiveData,
        defaultValue = initialViewState.checkupData.size > 1
    ) { viewState -> viewState.checkupData.size > 1 }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop {
            brushingForDaySingle(
                forDate.truncatedTo(ChronoUnit.DAYS).withHour(KOLIBREE_DAY_START_HOUR)
            ).subscribeOn(Schedulers.io())
                .subscribe(this::onBrushingData, Timber::e)
        }
    }

    fun onPageSelected(pageIndex: Int) {
        currentPosition = pageIndex
        updateViewStateForCurrentPosition()
    }

    override fun onBrushingDeleted(brushing: IBrushing) {
        sessionCache.get().apply {
            removeAll { it.iBrushing.dateTime == brushing.dateTime }

            if (isEmpty()) {
                onNoMoreBrushingData()
            } else {
                if (currentPosition >= sessionCache.get().size) {
                    currentPosition = sessionCache.get().size - 1
                }
                updateViewStateForCurrentPosition()
            }
        }
    }

    override fun currentBrushingSession() = sessionCache.get()[currentPosition].iBrushing

    private fun onNoMoreBrushingData() = pushAction(CheckupActions.FinishOk)

    private fun onBrushingData(sessions: List<IBrushing>) {
        val dayData = sessions.map(::toDayCheckupData)
        sessionCache.get()?.apply {
            clear()
            addAll(dayData)
        }

        updateViewStateForCurrentPosition()
    }

    private fun toDayCheckupData(brushing: IBrushing) = checkupCalculator
        .calculateCheckup(brushing).let { checkupData ->
            DayCheckupData(
                coverage = checkupData.coverage,
                durationPercentage = checkupData.duration.seconds / brushing.goalDuration.toFloat(),
                checkupData = checkupData.zoneSurfaceMap,
                iBrushing = brushing,
                duration = checkupData.duration.seconds
            )
        }

    private fun brushingForDaySingle(kolibreeDay: OffsetDateTime) =
        currentProfileProvider.currentProfileSingle()
            .flatMapObservable { profile ->
                brushingFacade.getBrushingSessions(
                    kolibreeDay,
                    kolibreeDay.plusDays(1),
                    profile.id
                )
            }.first(listOf())

    private fun updateViewStateForCurrentPosition() {
        val data = sessionCache.get()[currentPosition]
        updateViewState {
            copy(
                coverage = data.coverage,
                game = data.iBrushing.game,
                date = data.iBrushing.dateTime,
                durationPercentage = data.durationPercentage,
                durationSeconds = data.iBrushing.duration,
                checkupData = sessionCache.get().map(DayCheckupData::checkupData)
            )
        }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val currentProfileProvider: CurrentProfileProvider,
        private val brushingFacade: BrushingFacade,
        private val checkupCalculator: CheckupCalculator,
        private val forDate: OffsetDateTime
    ) : BaseViewModel.Factory<DayCheckupViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            DayCheckupViewModel(
                initialViewState = viewState ?: DayCheckupViewState.initial(),
                currentProfileProvider = currentProfileProvider,
                brushingFacade = brushingFacade,
                checkupCalculator = checkupCalculator,
                forDate = forDate
            ) as T
    }
}
