/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.common

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Created by miguelaragues on 24/4/18.  */
class BaseKolibreeServiceViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: StubBaseKolibreeServiceViewModel

    internal var serviceProvider: ServiceProvider = mock()

    @Throws(Exception::class)
    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
        viewModel = spy(StubBaseKolibreeServiceViewModel(serviceProvider))
    }

    /*
  ON START
   */
    @Test
    fun `multiple invocations to onStart only subscribe to connect once`() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())

        invokeOnStart()
        invokeOnStart()
        invokeOnStart()

        verify(serviceProvider).connectStream()

        assertEquals(1, viewModel.serviceStateDisposables.size().toLong())
    }

    @Test
    fun `onStart does not subscribe to connect if serviceConnectDisposable is not disposed`() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        viewModel.serviceConnectDisposable = mock()
        whenever(viewModel.serviceConnectDisposable.isDisposed).thenReturn(false)

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())

        invokeOnStart()

        assertFalse(connectSubject.hasObservers())

        verify(serviceProvider, never()).connectStream()

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())
    }

    @Test
    fun `onStart subscribes to connect if serviceConnectDisposable is disposed`() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        viewModel.serviceConnectDisposable = mock()
        whenever(viewModel.serviceConnectDisposable.isDisposed).thenReturn(true)

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())

        invokeOnStart()
        invokeOnStart()
        invokeOnStart()

        assertTrue(connectSubject.hasObservers())

        verify(serviceProvider).connectStream()

        assertEquals(1, viewModel.serviceStateDisposables.size().toLong())
    }

    @Test
    fun onStart_subscribesToConnect() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())

        invokeOnStart()

        verify(serviceProvider).connectStream()

        assertEquals(1, viewModel.serviceStateDisposables.size().toLong())
    }

    @Test
    fun `onStart invokes onServiceConnectionChanged when connectStream emits`() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        doNothing().whenever(viewModel).onServiceConnectionChanged(any())

        invokeOnStart()

        val serviceDisconnected = ServiceDisconnected
        connectSubject.onNext(serviceDisconnected)

        verify(viewModel).onServiceConnectionChanged(serviceDisconnected)

        val serviceConnected = ServiceConnected(mock())
        connectSubject.onNext(serviceConnected)

        verify(viewModel).onServiceConnectionChanged(serviceDisconnected)
    }

    @Test
    fun `onStart does not invoke onServiceConnectionChanged when connectStream emits duplicate`() {
        val connectSubject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        doNothing().whenever(viewModel).onServiceConnectionChanged(any())

        invokeOnStart()

        val serviceDisconnected = ServiceDisconnected
        connectSubject.onNext(serviceDisconnected)

        verify(viewModel).onServiceConnectionChanged(serviceDisconnected)

        connectSubject.onNext(serviceDisconnected)

        verify(viewModel).onServiceConnectionChanged(serviceDisconnected)
    }

    @Test
    fun onStart_connectError_doesNotCrash() {
        whenever(serviceProvider.connectStream()).thenReturn(Observable.error(TestForcedException()))

        doNothing().whenever(viewModel).onServiceConnectionChanged(any())

        invokeOnStart()
    }

    @Test
    fun `onStart nullifies service on observable disposal`() {
        val connectSubject =
            BehaviorSubject.createDefault<ServiceProvisionResult>(ServiceConnected(mock()))
        whenever(serviceProvider.connectStream()).thenReturn(connectSubject)

        invokeOnStart()

        assertNotNull(viewModel.kolibreeService)

        viewModel.serviceConnectDisposable.dispose()

        assertNull(viewModel.kolibreeService)
    }

    /*
  ON SERVICE CONNECTION CHANGED
   */
    @Test
    fun onServiceConnectionChanged_isConnectedFalse_invokesOnKolibreeServiceDisconnected() {
        doNothing().whenever(viewModel)
            .onKolibreeServiceDisconnected()

        viewModel.onServiceConnectionChanged(ServiceDisconnected)

        verify(viewModel).onKolibreeServiceDisconnected()
    }

    @Test
    fun onServiceConnectionChanged_isConnectedTrue_invokesOnKolibreeServiceConnected() {
        viewModel.kolibreeService = mock()

        viewModel.onServiceConnectionChanged(ServiceConnected(mock()))

        verify(viewModel).onKolibreeServiceConnected(viewModel.kolibreeService)
    }

    @Test
    fun `onServiceConnectionChanged stores KolibreeService on successful result`() {
        val expectedService = mock<KolibreeService>()
        val serviceConnected = ServiceConnected(expectedService)

        assertNull(viewModel.kolibreeService)

        viewModel.onServiceConnectionChanged(serviceConnected)

        assertEquals(expectedService, viewModel.kolibreeService)
    }

    /*
  ON KOLIBREE SERVICE DISCONNECTED
   */
    @Test
    fun onKolibreeServiceDisconnected_nullifiesService() {
        viewModel.kolibreeService = mock()

        viewModel.onKolibreeServiceDisconnected()

        assertNull(viewModel.kolibreeService)
    }

    /*
  ON KOLIBREE SERVICE CONNECTED
   */
    @Test
    fun onKolibreeServiceConnected_doesNothing() {
        viewModel.onKolibreeServiceConnected(mock())

        verify(viewModel).onKolibreeServiceConnected(any())
        verifyNoMoreInteractions(viewModel)
    }

    /*
  DISCONNECT FROM SERVICE
   */
    @Test
    fun disconnectFromService() {
        val disposable = mock<Disposable>()
        viewModel.serviceStateDisposables.add(disposable)

        assertEquals(1, viewModel.serviceStateDisposables.size().toLong())

        viewModel.disconnectFromService()

        verify(disposable).dispose()

        assertEquals(0, viewModel.serviceStateDisposables.size().toLong())
    }

    /*
  ON STOP
   */
    @Test
    fun onStop_canDisconnectFromServiceTrue_invokesDisconnectFromService() {
        doReturn(Observable.just(true))
            .whenever(viewModel).canDisconnectFromService()

        doNothing().whenever(viewModel).disconnectFromService()

        invokeOnStop()

        verify(viewModel).disconnectFromService()
    }

    @Test
    fun onStop_canDisconnectFromServiceFalseInitially_doesntInvokesDisconnectFromServiceUntilEmitTrue() {
        val subject = PublishSubject.create<Boolean>()
        doReturn(subject).whenever(viewModel)
            .canDisconnectFromService()

        invokeOnStop()

        subject.onNext(false)

        verify(viewModel, never()).disconnectFromService()

        subject.onNext(true)

        verify(viewModel).disconnectFromService()
    }

    /*
  CAN DISCONNECT FROM SERVICE
   */
    @Test
    fun canDisconnectFromService_returnsTrueForBaseImplementation() {
        viewModel.canDisconnectFromService().test().assertValues(true)
    }

    /*
  ON CLEARED
   */
    @Test
    fun onCleared_disposesCompositeDisposable() {
        assertFalse(viewModel.disposables.isDisposed)

        viewModel.onCleared()

        assertTrue(viewModel.disposables.isDisposed)
    }

    @Test
    fun onCleared_disposesServiceStateDisposable() {
        assertFalse(viewModel.serviceStateDisposables.isDisposed)

        viewModel.onCleared()

        assertTrue(viewModel.serviceStateDisposables.isDisposed)
    }

    /*
    Utils
     */

    private fun invokeOnStart() {
        viewModel.onStart(mock())
    }

    private fun invokeOnStop() {
        viewModel.onStop(mock())
    }

    private class StubBaseKolibreeServiceViewModel constructor(serviceProvider: ServiceProvider) :
        BaseKolibreeServiceViewModel(serviceProvider)
}
