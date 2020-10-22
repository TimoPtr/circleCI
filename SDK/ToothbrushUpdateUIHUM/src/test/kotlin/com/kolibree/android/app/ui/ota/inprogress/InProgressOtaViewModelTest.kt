/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota.inprogress

import androidx.lifecycle.Lifecycle
import com.jraska.livedata.test
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.ota.OtaUpdateNavigator
import com.kolibree.android.app.ui.ota.OtaUpdateParams
import com.kolibree.android.app.ui.ota.OtaUpdateSharedViewModel
import com.kolibree.android.app.ui.ota.OtaUpdater
import com.kolibree.android.app.ui.ota.R
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.OtaUpdateEvent
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.pushLifecycleTo
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class InProgressOtaViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: InProgressOtaViewModel

    private val navigator: OtaUpdateNavigator = mock()

    private val sharedViewState: OtaUpdateSharedViewModel = mock()

    private val otaUpdateParams: OtaUpdateParams =
        OtaUpdateParams(false, MAC, ToothbrushModel.CONNECT_B1)
    private val serviceProvider: ServiceProvider = mock()
    private val otaUpdater: OtaUpdater = mock()

    private val connection =
        KLTBConnectionBuilder.createAndroidLess().withMac(MAC)
            .withModel(ToothbrushModel.CONNECT_B1)
            .build()

    override fun setup() {
        super.setup()

        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        whenever(service.getConnection(MAC)).thenReturn(connection)

        viewModel =
            InProgressOtaViewModel(
                null,
                sharedViewState,
                navigator,
                otaUpdateParams,
                serviceProvider,
                otaUpdater
            )
    }

    @Test
    fun `progress maps progress from view state`() {
        val testObserver = viewModel.progress.test()

        viewModel.updateViewState { copy(progress = 50) }

        testObserver.assertValue(50)
    }

    @Test
    fun `showResult maps showResult from view state`() {
        val testObserver = viewModel.showResult.test()

        viewModel.updateViewState { copy(isOtaSuccess = true) }

        testObserver.assertValue(true)
    }

    @Test
    fun `isOtaFailed maps isOtaFailed from view state`() {
        val testObserver = viewModel.isOtaFailed.test()

        viewModel.updateViewState { copy(isOtaFailed = true) }

        testObserver.assertValue(true)
    }

    @Test
    fun `resultIcon maps resultIcon from view state`() {
        val testObserver = viewModel.resultIcon.test()

        viewModel.updateViewState { copy(isOtaFailed = true) }

        testObserver.assertValue(R.drawable.ic_ota_fail)
    }

    @Test
    fun `title maps title from view state`() {
        val testObserver = viewModel.title.test()

        viewModel.updateViewState { copy(isOtaFailed = true) }

        testObserver.assertValue(R.string.ota_failure_title)
    }

    @Test
    fun `content maps content from view state`() {
        val testObserver = viewModel.content.test()

        viewModel.updateViewState { copy(isOtaFailed = true) }

        testObserver.assertValue(R.string.ota_failure_content)
    }

    @Test
    fun `when the user click on done then app send analytics event`() {
        viewModel.onDoneClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_UpdateFailed_done"))

        viewModel.updateViewState { copy(isOtaSuccess = true) }

        viewModel.onDoneClick()

        verify(eventTracker).sendEvent(AnalyticsEvent("TBSettings_PopUpdate_Done"))
    }

    @Test
    fun `when the user click on done it close the screen`() {
        viewModel.onDoneClick()

        verify(navigator).finishScreen()
    }

    @Test
    fun `when the screen appear ota start and update progress`() {
        val subject = PublishSubject.create<OtaUpdateEvent>()
        whenever(otaUpdater.updateToothbrushObservable(connection)).thenReturn(subject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        subject.onNext(OtaUpdateEvent(0, progress = 10))

        assertEquals(10, viewModel.getViewState()?.progress)

        subject.onNext(OtaUpdateEvent(0, progress = 15))

        assertEquals(15, viewModel.getViewState()?.progress)
    }

    @Test
    fun `when ota in progress and an error occur it set ota to failure`() {
        val subject = PublishSubject.create<OtaUpdateEvent>()
        whenever(otaUpdater.updateToothbrushObservable(connection)).thenReturn(subject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        subject.onError(IllegalStateException())

        assertTrue(viewModel.getViewState()!!.isOtaFailed)
    }

    @Test
    fun `when ota in done it set ota to done and reset tag of the connection`() {
        connection.tag = "hello"
        val subject = PublishSubject.create<OtaUpdateEvent>()
        whenever(otaUpdater.updateToothbrushObservable(connection)).thenReturn(subject)

        viewModel.pushLifecycleTo(Lifecycle.Event.ON_CREATE)

        subject.onComplete()

        assertTrue(viewModel.getViewState()!!.isOtaSuccess)
        assertEquals(100, viewModel.getViewState()!!.progress)
        assertNull(connection.tag)
    }
}

private const val MAC = "mac"
