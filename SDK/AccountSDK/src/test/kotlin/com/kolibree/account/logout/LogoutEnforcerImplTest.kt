/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.logout

import android.app.Application
import android.content.Context
import com.kolibree.account.eraser.UserSessionManager
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class LogoutEnforcerImplTest : BaseUnitTest() {
    private val context: Context = mock()
    private val connector: InternalKolibreeConnector = mock()
    private val application: Application = mock()
    private val shouldLogoutUseCase: ShouldLogoutUseCase = mock()
    private val intentAfterForcedLogout: IntentAfterForcedLogout = mock()
    private val userSessionManager: UserSessionManager = mock()
    private val createdActivitiesWatcher: CreatedActivitiesWatcher = mock()

    private lateinit var logoutEnforcer: LogoutEnforcerImpl

    override fun setup() {
        super.setup()

        whenever(context.applicationContext).thenReturn(application)
    }

    /*
    onUserLoggedIn
     */
    @Test
    fun `onUserLoggedIn invokes initWatch`() {
        initWitNullIntent()

        doNothing().whenever(logoutEnforcer).initWatch()

        logoutEnforcer.onUserLoggedIn()

        verify(logoutEnforcer).initWatch()
    }

    /*
    onUserLoggedOut
     */
    @Test
    fun `onUserLoggedOut invokes stopWatch`() {
        initWitNullIntent()

        doNothing().whenever(logoutEnforcer).stopWatch()

        logoutEnforcer.onUserLoggedOut()

        verify(logoutEnforcer).stopWatch()
    }

    /*
    initWatch
     */
    @Test
    fun `initWatch does nothing if canStartWatch returns false`() {
        initWithNonNullIntent()

        doReturn(false).whenever(logoutEnforcer).canStartWatch()

        logoutEnforcer.initWatch()

        verify(shouldLogoutUseCase, never()).shouldLogoutStream
    }

    @Test
    fun `initWatch subscribes to shouldLogoutStream if canStartWatch returns true`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).canStartWatch()

        val subject = PublishSubject.create<ForceLogoutReason>()
        whenever(shouldLogoutUseCase.shouldLogoutStream).thenReturn(subject)

        assertFalse(subject.hasObservers())

        logoutEnforcer.initWatch()

        verify(shouldLogoutUseCase).shouldLogoutStream

        assertTrue(subject.hasObservers())
    }

    @Test
    fun `initWatch invokes startWatchingActivities after subscribing`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).canStartWatch()

        val subject = PublishSubject.create<ForceLogoutReason>()
        whenever(shouldLogoutUseCase.shouldLogoutStream).thenReturn(subject)

        logoutEnforcer.initWatch()

        verify(logoutEnforcer).startWatchingActivities()
    }

    @Test
    fun `initWatch unsubscribes from shouldLogoutStream as soon as it emits 1 value`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).canStartWatch()

        val subject = PublishSubject.create<ForceLogoutReason>()
        whenever(shouldLogoutUseCase.shouldLogoutStream).thenReturn(subject)

        logoutEnforcer.initWatch()

        assertTrue(subject.hasObservers())

        doNothing().whenever(logoutEnforcer).onForcedLogout(any())
        doNothing().whenever(logoutEnforcer).stopWatch()

        subject.onNext(AccountDoesNotExist)

        assertFalse(subject.hasObservers())
    }

    @Test
    fun `initWatch invokes onForcedLogout and then stopWatch as shouldLogoutStream emits 1 value`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).canStartWatch()

        val subject = PublishSubject.create<ForceLogoutReason>()
        whenever(shouldLogoutUseCase.shouldLogoutStream).thenReturn(subject)

        logoutEnforcer.initWatch()

        doNothing().whenever(logoutEnforcer).onForcedLogout(any())
        doNothing().whenever(logoutEnforcer).stopWatch()

        subject.onNext(AccountDoesNotExist)

        inOrder(logoutEnforcer) {
            verify(logoutEnforcer).onForcedLogout(AccountDoesNotExist)
            verify(logoutEnforcer).stopWatch()
        }
    }

    /*
    shouldManageNavigation
     */
    @Test
    fun `shouldManageActivities returns true if intentAfterForcedLogout is not null`() {
        initWithNonNullIntent()

        assertNull(logoutEnforcer.forceLogoutDisposable)

        assertTrue(logoutEnforcer.shouldManageNavigation())
    }

    @Test
    fun `shouldManageActivities returns false if intentAfterForcedLogout is null`() {
        initWitNullIntent()

        assertNull(logoutEnforcer.forceLogoutDisposable)

        assertFalse(logoutEnforcer.shouldManageNavigation())
    }

    /*
    canStartWatch
     */
    @Test
    fun `canStartWatch returns true if forceLogoutDisposable is null`() {
        initWithNonNullIntent()

        assertNull(logoutEnforcer.forceLogoutDisposable)

        assertTrue(logoutEnforcer.canStartWatch())
    }

    @Test
    fun `canStartWatch returns true if forceLogoutDisposable is disposed`() {
        initWithNonNullIntent()

        logoutEnforcer.forceLogoutDisposable = mock()

        whenever(logoutEnforcer.forceLogoutDisposable!!.isDisposed).thenReturn(true)

        assertTrue(logoutEnforcer.canStartWatch())
    }

    @Test
    fun `canStartWatch returns false if forceLogoutDisposable is not disposed`() {
        initWithNonNullIntent()

        logoutEnforcer.forceLogoutDisposable = mock()

        whenever(logoutEnforcer.forceLogoutDisposable!!.isDisposed).thenReturn(false)

        assertFalse(logoutEnforcer.canStartWatch())
    }

    /*
    startWatchingActivities
     */
    @Test
    fun `startWatchingActivities does nothing if shouldManageActivities returns false`() {
        initWithNonNullIntent()

        doReturn(false).whenever(logoutEnforcer).shouldManageNavigation()

        logoutEnforcer.startWatchingActivities()

        verify(createdActivitiesWatcher, never()).clear()
        verify(application, never()).registerActivityLifecycleCallbacks(createdActivitiesWatcher)
    }

    @Test
    fun `startWatchingActivities invokes clear on createdActivitiesWatcher before registering activity lifecycle callback`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).shouldManageNavigation()

        logoutEnforcer.startWatchingActivities()

        inOrder(createdActivitiesWatcher, application) {
            verify(createdActivitiesWatcher).clear()

            verify(application).registerActivityLifecycleCallbacks(createdActivitiesWatcher)
        }
    }

    /*
    stopWatch
     */
    @Test
    fun `stopWatchingActivities unregisters activity lifecycle callbacks if shouldManageActivities returns true`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).shouldManageNavigation()

        logoutEnforcer.stopWatchingActivities()

        verify(application).unregisterActivityLifecycleCallbacks(createdActivitiesWatcher)
    }

    @Test
    fun `stopWatchingActivities does nothing if shouldManageActivities returns false`() {
        initWithNonNullIntent()

        doReturn(false).whenever(logoutEnforcer).shouldManageNavigation()

        logoutEnforcer.stopWatchingActivities()

        verify(application, never()).unregisterActivityLifecycleCallbacks(createdActivitiesWatcher)
    }

    /*
    stopWatch
     */
    @Test
    fun `stopWatch invokes stopWatchingActivities`() {
        initWithNonNullIntent()

        doNothing().whenever(logoutEnforcer).stopWatchingActivities()

        logoutEnforcer.stopWatch()

        verify(logoutEnforcer).stopWatchingActivities()
    }

    @Test
    fun `stopWatch disposes forceLogoutDisposable`() {
        initWithNonNullIntent()

        val disposable = mock<Disposable>()
        logoutEnforcer.forceLogoutDisposable = disposable

        logoutEnforcer.stopWatch()

        verify(disposable).dispose()
    }

    @Test
    fun `stopWatch nullifies forceLogoutDisposable`() {
        initWithNonNullIntent()

        logoutEnforcer.forceLogoutDisposable = mock()

        logoutEnforcer.stopWatch()

        assertNull(logoutEnforcer.forceLogoutDisposable)
    }

    /*
    forceNavigation
     */
    @Test
    fun `forceNavigation unregisters activity lifecycle callbacks`() {
        initWithNonNullIntent()

        doNothing().whenever(logoutEnforcer).navigateToAfterLogoutScreen(any())

        logoutEnforcer.forceNavigation(AccountDoesNotExist)

        verify(application).unregisterActivityLifecycleCallbacks(createdActivitiesWatcher)
    }

    @Test
    fun `forceNavigation invokes navigateToAfterLogoutScreen with ForceLogoutReason parameter`() {
        initWithNonNullIntent()

        doNothing().whenever(logoutEnforcer).navigateToAfterLogoutScreen(any())

        logoutEnforcer.forceNavigation(AccountDoesNotExist)

        verify(logoutEnforcer).navigateToAfterLogoutScreen(AccountDoesNotExist)
    }

    /*
    We need to make sure we stop watching before starting the new Activity, otherwise we risk finishing
    the just started activity
     */
    @Test
    fun `forceNavigation invokes stopWatching, navigateToAfterLogoutScreen and finishActivitiesReverseOrder in order`() {
        initWithNonNullIntent()

        doNothing().whenever(logoutEnforcer).navigateToAfterLogoutScreen(any())

        logoutEnforcer.forceNavigation(AccountDoesNotExist)

        inOrder(application, logoutEnforcer, createdActivitiesWatcher) {
            verify(application).unregisterActivityLifecycleCallbacks(createdActivitiesWatcher)

            verify(logoutEnforcer).navigateToAfterLogoutScreen(AccountDoesNotExist)

            verify(createdActivitiesWatcher).finishActivitiesReverseOrder()
        }
    }

    /*
    onForcedLogout
     */

    @Test
    fun `onForcedLogout invokes forceNavigation if shouldManageActivities returns true`() {
        initWithNonNullIntent()

        doReturn(true).whenever(logoutEnforcer).shouldManageNavigation()

        doNothing().whenever(logoutEnforcer).forceNavigation(any())

        logoutEnforcer.onForcedLogout(AccountDoesNotExist)

        verify(logoutEnforcer).forceNavigation(AccountDoesNotExist)
    }

    @Test
    fun `onForcedLogout never invokes forceNavigation if shouldManageActivities returns false`() {
        initWithNonNullIntent()

        doReturn(false).whenever(logoutEnforcer).shouldManageNavigation()

        logoutEnforcer.onForcedLogout(AccountDoesNotExist)

        verify(logoutEnforcer, never()).forceNavigation(AccountDoesNotExist)
    }

    @Test
    fun `onForcedLogout resets userSession`() {
        initWithNonNullIntent()

        doNothing().whenever(logoutEnforcer).navigateToAfterLogoutScreen(any())

        logoutEnforcer.onForcedLogout(AccountDoesNotExist)

        verify(userSessionManager).reset()
    }

    /*
    navigateToAfterLogoutScreen
     */

    @Test
    fun `navigateToAfterLogoutScreen does nothing if intentAfterForcedLogout is null`() {
        initWitNullIntent()

        logoutEnforcer.navigateToAfterLogoutScreen(AccountDoesNotExist)

        verifyNoMoreInteractions(intentAfterForcedLogout)
        verifyNoMoreInteractions(application)
    }

    @Test
    fun `navigateToAfterLogoutScreen adds ForceLogoutReason as Extra`() {
        initWithNonNullIntent()

        logoutEnforcer.navigateToAfterLogoutScreen(AccountDoesNotExist)

        verify(intentAfterForcedLogout).putExtra(EXTRA_FORCED_LOGOUT, AccountDoesNotExist)
    }

    @Test
    fun `navigateToAfterLogoutScreen invokes startActivity on intent after adding flags`() {
        initWithNonNullIntent()

        logoutEnforcer.navigateToAfterLogoutScreen(AccountDoesNotExist)

        inOrder(intentAfterForcedLogout, application) {
            verify(intentAfterForcedLogout).putExtra(
                eq(EXTRA_FORCED_LOGOUT),
                any<ForceLogoutReason>()
            )

            verify(application).startActivity(intentAfterForcedLogout)
        }
    }

    /*
    UTILS
     */

    private fun initWithNonNullIntent() = init(intent = intentAfterForcedLogout)

    private fun initWitNullIntent() = init()

    private fun init(intent: IntentAfterForcedLogout? = null) {
        logoutEnforcer = spy(
            LogoutEnforcerImpl(
                context,
                shouldLogoutUseCase,
                userSessionManager,
                createdActivitiesWatcher,
                intent
            )
        )
    }
}
