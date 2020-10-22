/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.checkup.base

import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.home.tab.home.card.lastbrushing.BrushingType
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.android.parcel.Parcelize
import org.junit.Test
import org.threeten.bp.OffsetDateTime

/** [BaseCheckupViewModel] unit tests */
class BaseCheckupViewModelTest : BaseUnitTest() {

    private val brushingFacade = mock<BrushingFacade>()

    private lateinit var viewModel: BaseCheckupViewModel<BaseCheckupViewStateStub>

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel =
            BaseCheckupViewModel(
                initialViewState = BaseCheckupViewStateStub(),
                brushingFacade = brushingFacade
            )
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    /*
    onBackButtonClick
     */

    @Test
    fun `onBackButtonClick pushes FinishCancel`() {
        val testObserver = viewModel.actionsObservable.test()

        viewModel.onBackButtonClick()

        testObserver.assertLastValue(CheckupActions.FinishCancel)
    }

    /*
    coverageLiveData
     */

    @Test
    fun `coverageLiveData emits state's coverage`() {
        val expectedCoverage = 0.45f
        val testObserver = viewModel.coverageLiveData.test()

        viewModel.updateViewState { copy(coverage = expectedCoverage) }

        testObserver.assertValue(expectedCoverage)
    }

    /*
    durationLiveData
     */

    @Test
    fun `durationLiveData emits state's duration`() {
        val expectedDuration = 0.45f
        val testObserver = viewModel.durationPercentageLiveData.test()

        viewModel.updateViewState { copy(durationPercentage = expectedDuration) }

        testObserver.assertValue(expectedDuration)
    }

    /*
    durationSecondsLiveData
     */

    @Test
    fun `durationSecondsLiveData emits state's duration`() {
        val expectedDurationSeconds = 23L
        val testObserver = viewModel.durationSecondsLiveData.test()

        viewModel.updateViewState { copy(durationSeconds = expectedDurationSeconds) }

        testObserver.assertValue(expectedDurationSeconds)
    }

    /*
    brushingTypeLiveData
     */

    @Test
    fun `brushingTypeLiveData emits state's duration`() {
        val testObserver = viewModel.brushingTypeLiveData.test()

        viewModel.updateViewState { copy(game = "co+") }

        testObserver.assertValue(BrushingType.GuidedBrushing)
    }

    /*
    brushingDateLiveData
     */

    @Test
    fun `brushingDateLiveData emits state's duration`() {
        val expectedDate = TrustedClock.getNowOffsetDateTime()
        val testObserver = viewModel.brushingDateLiveData.test()

        viewModel.updateViewState { copy(date = expectedDate) }

        testObserver.assertValue(expectedDate)
    }

    /*
    onDeleteConfirmed
     */

    @Test
    fun `onDeleteConfirmed deletes current brushing session`() {
        val viewModelSpy = spy(viewModel)
        val expectedBrushing = mock<IBrushing>()
        doReturn(expectedBrushing).whenever(viewModelSpy).currentBrushingSession()

        whenever(brushingFacade.deleteBrushing(expectedBrushing))
            .thenReturn(Completable.complete())
        whenever(brushingFacade.getBrushings(any()))
            .thenReturn(Single.just(listOf(expectedBrushing)))

        viewModelSpy.onDeleteConfirmed()

        verify(brushingFacade).deleteBrushing(expectedBrushing)
    }
}

@Parcelize
private data class BaseCheckupViewStateStub(
    override val coverage: Float? = null,
    override val durationPercentage: Float = 0f,
    override val durationSeconds: Long = 0L,
    override val game: String? = null,
    override val date: OffsetDateTime = TrustedClock.getNowOffsetDateTime()
) : BaseCheckupViewState
