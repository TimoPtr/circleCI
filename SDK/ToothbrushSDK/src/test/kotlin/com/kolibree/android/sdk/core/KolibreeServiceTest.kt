package com.kolibree.android.sdk.core

import android.content.Intent
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor

class KolibreeServiceTest : BaseUnitTest() {
    private val service = spy(KolibreeService())

    override fun setup() {
        super.setup()
        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    /*
    init
     */
    @Test
    fun `init invokes createBinder`() {
        doNothing().whenever(service).createConnections()
        doNothing().whenever(service).createBinder()

        mockBackgroundJobManagers()

        service.init()

        verify(service).createBinder()
    }

    @Test
    fun `init invokes createConnections if there are no backgroundJobManagers`() {
        doNothing().whenever(service).createConnections()
        doNothing().whenever(service).createBinder()

        mockBackgroundJobManagers()

        service.init()

        verify(service).createConnections()
    }

    @Test
    fun `init invokes cancel on all backgroundJobManagers before creating connections`() {
        doNothing().whenever(service).createConnections()
        doNothing().whenever(service).createBinder()

        val jobManager1 = mock<BackgroundJobManager>()
        val jobManager2 = mock<BackgroundJobManager>()
        mockBackgroundJobManagers(jobManager1, jobManager2)

        service.init()

        inOrder(jobManager1, jobManager2, service) {
            verify(jobManager1).cancelJob()
            verify(jobManager2).cancelJob()
            verify(service).createConnections()
        }
    }

    /*
    Binding
     */

    @Test(expected = IllegalAccessError::class)
    fun verifyClient_noBindingKey_throwsIllegalArgumentException() {
        val intent: Intent = mock()

        whenever(intent.getBooleanExtra(eq(KolibreeService.KOLIBREE_BINDING_EXTRA), any<Boolean>()))
            .thenReturn(false)

        service.verifyClient(intent)
    }

    @Test
    fun verifyClient_withBindingKey_neverThrowsIllegalArgumentException() {
        val intent: Intent = mock()

        whenever(intent.getBooleanExtra(eq(KolibreeService.KOLIBREE_BINDING_EXTRA), any<Boolean>()))
            .thenReturn(true)

        service.verifyClient(intent)
    }

    /*
    CLOSE CONNECTIONS
     */
    @Test
    fun closeConnections_disposesCompositeDisposable() {
        val disposable = PublishSubject.create<Any>().subscribe()

        service.disposables.add(disposable)

        service.kltbConnectionPoolManager = mock()

        assertFalse(disposable.isDisposed)

        service.closeConnections()

        assertTrue(disposable.isDisposed)
    }

    @Test
    fun closeConnections_invokesKLTBConnectionPoolManagerClose() {
        val disposable = PublishSubject.create<Any>().subscribe()

        service.disposables.add(disposable)

        service.kltbConnectionPoolManager = mock()

        service.closeConnections()

        verify(service.kltbConnectionPoolManager).close()
    }

    /*
    CREATE CONNECTIONS
     */
    @Test
    fun `createConnections invokes refreshForegroundState on complete`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.init()).thenReturn(Completable.complete())

        service.createConnections()

        verify(service).refreshForegroundState()
    }

    /*
    FORGET COMPLETABLEMAC
     */
    @Test
    fun `forgetCompletable subscribes to pool forget`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        val mac = "bla"
        val poolForgetSubject = CompletableSubject.create()
        whenever(connectionPoolManager.forget(mac)).thenReturn(poolForgetSubject)

        val observable = service.forgetCompletable(mac)

        assertFalse(poolForgetSubject.hasObservers())

        observable.test()

        assertTrue(poolForgetSubject.hasObservers())
    }

    @Test
    fun `forgetCompletable mac invokes refreshForegroundState on complete`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        val mac = "bla"
        whenever(connectionPoolManager.forget(mac)).thenReturn(Completable.complete())

        val observable = service.forgetCompletable(mac)

        verify(service, never()).refreshForegroundState()

        observable.test()

        verify(service).refreshForegroundState()
    }

    @Test
    fun `forgetCompletable completes successfully if pool throws UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        val mac = "bla"
        val unknownToothbrushException = UnknownToothbrushException(mac)
        whenever(connectionPoolManager.forget(mac))
            .thenReturn(Completable.error(unknownToothbrushException))

        service.forgetCompletable(mac).test().assertComplete().assertNoErrors()
    }

    @Test
    fun `forgetCompletable emits error if pool throws error different than UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        val mac = "bla"
        val expectedException = Exception(mac)
        whenever(connectionPoolManager.forget(mac)).thenReturn(Completable.error(expectedException))

        service.forgetCompletable(mac).test().assertError(expectedException)
    }

    /*
    FORGET MAC
     */
    @Test
    fun `forget mac invokes refreshForegroundState on complete`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<String>())).thenReturn(Completable.complete())

        service.forget("bla")

        verify(service).refreshForegroundState()
    }

    @Test
    fun `forget mac invokes refreshForegroundState on complete even if pool forget throws UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<String>()))
            .thenReturn(Completable.error(UnknownToothbrushException("da")))

        service.forget("bla")

        verify(service).refreshForegroundState()
    }

    @Test
    fun `forget mac never invokes refreshForegroundState on complete if pool forget throws exception different than UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<String>()))
            .thenReturn(Completable.error(TestForcedException()))

        service.forget("bla")

        verify(service, never()).refreshForegroundState()
    }

    /*
    FORGET CONNECTION
     */
    @Test
    fun `forget connection invokes refreshForegroundState on complete`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<KLTBConnection>()))
            .thenReturn(Completable.complete())

        service.forget(mock<KLTBConnection>())

        verify(service).refreshForegroundState()
    }

    @Test
    fun `forget connection invokes refreshForegroundState on complete even if pool forget throws UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<KLTBConnection>()))
            .thenReturn(Completable.error(UnknownToothbrushException("da")))

        service.forget(mock<KLTBConnection>())

        verify(service).refreshForegroundState()
    }

    @Test
    fun `forget connection never invokes refreshForegroundState on complete if pool forget throws exception different than UnknownToothbrushException`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        whenever(connectionPoolManager.forget(any<KLTBConnection>()))
            .thenReturn(Completable.error(TestForcedException()))

        service.forget(mock<KLTBConnection>())

        verify(service, never()).refreshForegroundState()
    }

    /*
    CREATE AND ESTABLISH CONNECTION SCAN RESULT
     */
    @Test
    fun `createAndEstablishConnection scan result invokes refreshForegroundState`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        service.createAndEstablishConnection(mock())

        verify(service).refreshForegroundState()
    }

    /*
    CREATE AND ESTABLISH CONNECTION
     */
    @Test
    fun `createAndEstablishConnection result invokes refreshForegroundState`() {
        doNothing().whenever(service).refreshForegroundState()

        val connectionPoolManager = mockConnectionPoolManager()
        service.kltbConnectionPoolManager = connectionPoolManager

        service.createAndEstablishConnection("mac", mock(), "name")

        verify(service).refreshForegroundState()
    }

    /*
    REFRESH FOREGROUND STATE
     */
    @Test
    fun `refreshForegroundState invokes stopForeground when there are no connections`() {
        doAnswer {
        }.whenever(service).runOnMainThread(any())

        val captor = ArgumentCaptor.forClass(Runnable::class.java)

        doNothing().whenever(service).stopServiceAsForeground()

        doReturn(listOf<KLTBConnection>()).whenever(service).knownConnections

        service.refreshForegroundState()

        verify(service).runOnMainThread(captor.capture())

        captor.value.run()

        verify(service).stopServiceAsForeground()
    }

    @Test
    fun `refreshForegroundState invokes startServiceAsForeground when there are connections`() {
        doAnswer {
        }.whenever(service).runOnMainThread(any())

        val captor = ArgumentCaptor.forClass(Runnable::class.java)

        doNothing().whenever(service).startServiceAsForeground()

        doReturn(listOf<KLTBConnection>(mock())).whenever(service).knownConnections

        service.refreshForegroundState()

        verify(service).runOnMainThread(captor.capture())

        captor.value.run()
        verify(service).startServiceAsForeground()
    }

    /*
    Utils
     */

    private fun mockConnectionPoolManager() = mock<InternalKLTBConnectionPoolManager>()

    private fun mockBackgroundJobManagers(vararg backgroundJobManager: BackgroundJobManager) {
        service.backgroundJobManagerSet = backgroundJobManager.toSet()
    }
}
