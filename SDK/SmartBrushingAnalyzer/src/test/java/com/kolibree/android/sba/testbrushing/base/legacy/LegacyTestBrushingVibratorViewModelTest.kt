/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.base.legacy

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sba.testbrushing.base.NoneAction
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.connection.vibrator.Vibrator
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test

class LegacyTestBrushingVibratorViewModelTest : BaseUnitTest() {

    private val initViewState = InstanceViewState()

    internal lateinit var viewModel: LegacyTestBrushingVibratorViewModel<InstanceViewState>

    internal val serviceProvider = mock<ServiceProvider>()

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        viewModel = spy(InstanceVibratorViewModel(MAC, serviceProvider, initViewState))
    }

    @Test
    fun onStart_invokesServiceProvidedSuccess() {
        val service = mock<KolibreeService>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        doNothing().whenever(viewModel).serviceProvidedSuccess(service)

        viewModel.onStart(mock())

        verify(viewModel).serviceProvidedSuccess(service)
    }

    @Test
    fun serviceProvidedSuccess_invokesGetConnection() {
        val connection = mock<KLTBConnection>()
        val service = mock<KolibreeService>()
        val vibrator = mock<Vibrator>()
        val connectionState = mock<ConnectionState>()
        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        whenever(service.getConnection(MAC)).thenReturn(connection)
        whenever(connection.state()).thenReturn(connectionState)
        whenever(connection.vibrator()).thenReturn(vibrator)

        viewModel.serviceProvidedSuccess(service)

        verify(service).getConnection(MAC)
    }

    @Test
    fun serviceProvidedSuccess_noConnection_doNothing() {
        val service = mock<KolibreeService>()
        whenever(service.getConnection(MAC)).thenReturn(null)

        Assert.assertNull(viewModel.registeredVibrator)

        viewModel.serviceProvidedSuccess(service)

        Assert.assertNull(viewModel.registeredVibrator)
    }

    @Test
    fun serviceProvidedSuccess_hasConnection_invokesRegisterVibrator() {
        val connection = mock<KLTBConnection>()
        val service = mock<KolibreeService>()
        val vibrator = mock<Vibrator>()
        val connectionState = mock<ConnectionState>()

        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        whenever(service.getConnection(MAC)).thenReturn(connection)
        whenever(connection.state()).thenReturn(connectionState)
        whenever(connection.vibrator()).thenReturn(vibrator)
        val listener = viewModel.vibratorListener
        doNothing().whenever(vibrator).register(listener)

        viewModel.serviceProvidedSuccess(service)

        verify(vibrator).register(viewModel.vibratorListener)
    }

    @Test
    fun serviceProvidedSuccess_hasConnection_assignConnection() {
        val connection = mock<KLTBConnection>()
        val service = mock<KolibreeService>()
        val vibrator = mock<Vibrator>()
        val connectionState = mock<ConnectionState>()

        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))
        whenever(service.getConnection(MAC)).thenReturn(connection)
        whenever(connection.vibrator()).thenReturn(vibrator)
        whenever(connection.state()).thenReturn(connectionState)

        Assert.assertNull(viewModel.registeredVibrator)

        viewModel.serviceProvidedSuccess(service)

        Assert.assertEquals(vibrator, viewModel.registeredVibrator)
    }

    @Test
    fun onStop_invokesUnregisterVibrator() {
        doNothing().whenever(viewModel).unregisterVibrator()

        viewModel.onStop(mock())

        verify(viewModel).unregisterVibrator()
    }

    internal class InstanceVibratorViewModel(
        mac: String,
        serviceProvider: ServiceProvider,
        viewState: InstanceViewState
    ) : LegacyTestBrushingVibratorViewModel<InstanceViewState>(mac, serviceProvider, viewState) {

        override fun resetActionViewState(): InstanceViewState = InstanceViewState()

        override fun onVibratorStateChanged(isVibratorOn: Boolean) {}
    }

    internal class InstanceViewState : LegacyBaseTestBrushingViewState(NoneAction)

    companion object {
        private const val MAC = "01:02:03:04:FF"
    }
}
