/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings

import com.kolibree.android.app.interactor.KolibreeServiceInteractor
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.settings.persistence.repo.CoachSettingsRepository
import com.kolibree.android.sdk.e1.ToothbrushShutdownValve
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase
import org.junit.Test

internal class BaseSettingsViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: FakeBaseSettingsViewModel

    private val connector: IKolibreeConnector = mock()
    private val coachSettingsRepository: CoachSettingsRepository = mock()
    private val shutdownValve: ToothbrushShutdownValve = mock()

    private val kolibreeServiceInteractor = mock<KolibreeServiceInteractor>()

    override fun setup() {
        super.setup()

        whenever(connector.currentProfile).thenReturn(ProfileBuilder.create().build())

        viewModel = FakeBaseSettingsViewModel(
            coachSettingsRepository,
            connector,
            kolibreeServiceInteractor,
            shutdownValve
        )
    }

    /*
    onStart
     */
    @Test
    fun `onStart invokes preventToothbrushShutdown`() {
        spyViewModel()

        doNothing().whenever(viewModel).preventToothbrushShutdown()
        doNothing().whenever(viewModel).readSettings()

        viewModel.onStart(mock())

        verify(viewModel).preventToothbrushShutdown()
    }

    @Test
    fun `onStart invokes readSettings`() {
        spyViewModel()

        doNothing().whenever(viewModel).preventToothbrushShutdown()
        doNothing().whenever(viewModel).readSettings()

        viewModel.onStart(mock())

        verify(viewModel).readSettings()
    }

    /*
    preventToothbrushShutdown
     */
    @Test
    fun `preventToothbrushShutdown invokes preventShutdown on shutdownValve`() {
        val subject = CompletableSubject.create()
        whenever(shutdownValve.preventShutdownValve()).thenReturn(subject)

        viewModel.preventToothbrushShutdown()

        TestCase.assertTrue(subject.hasObservers())
    }

    @Test
    fun `preventToothbrushShutdown stores disposable`() {
        val subject = CompletableSubject.create()
        whenever(shutdownValve.preventShutdownValve()).thenReturn(subject)

        TestCase.assertNull(viewModel.preventToothbrushShutdownDisposable)

        viewModel.preventToothbrushShutdown()

        TestCase.assertNotNull(viewModel.preventToothbrushShutdownDisposable)
    }

    @Test
    fun `preventToothbrushShutdown disposes previews preventShutdown subscriber`() {
        val subject = CompletableSubject.create()
        whenever(shutdownValve.preventShutdownValve()).thenReturn(subject)

        val previousSubscriber = mock<Disposable>()
        viewModel.preventToothbrushShutdownDisposable = previousSubscriber

        viewModel.preventToothbrushShutdown()

        verify(previousSubscriber).dispose()
    }

    /*
    onStop
     */
    @Test
    fun `onStop disposes preventToothbrushShutdownDisposable`() {
        viewModel.preventToothbrushShutdownDisposable = mock()

        viewModel.onStop(mock())

        verify(viewModel.preventToothbrushShutdownDisposable)!!.dispose()
    }

    @Test
    fun `onStop does not crash if preventToothbrushShutdownDisposable is null`() {
        TestCase.assertNull(viewModel.preventToothbrushShutdownDisposable)

        viewModel.onStop(mock())
    }

    /*
    Utils
     */
    private fun spyViewModel() {
        viewModel = spy(viewModel)
    }
}

private class FakeBaseSettingsViewModel(
    settingsRepository: CoachSettingsRepository,
    connector: IKolibreeConnector,
    serviceInteractor: KolibreeServiceInteractor,
    shutdownValve: ToothbrushShutdownValve
) : BaseSettingsViewModel<Any>(
    settingsRepository,
    connector,
    serviceInteractor,
    shutdownValve
) {
    override fun initialViewState(): Any = Object()

    override fun createViewStateFromSettings(): Any = Object()
}
