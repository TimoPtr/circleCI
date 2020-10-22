/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.googlewallet

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.feature.GooglePayFeature
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.shop.data.googlewallet.requests.IsReadyToPayRequestUseCase
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.android.test.utils.TestFeatureToggle
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import timber.log.Timber

internal class GooglePayAvailabilityWatcherImplTest : BaseUnitTest() {
    private val activity: AppCompatActivity = mock()
    private val cartRepository: CartRepository = mock()
    private val isReadyToPayRequestUseCase: IsReadyToPayRequestUseCase = mock()
    private val sessionFlags: SessionFlags = mock()
    private val lifecycle: Lifecycle = mock()

    // initialValue is true in order to let the other test pass
    private val featureToggles = TestFeatureToggle(GooglePayFeature, initialValue = true)

    private lateinit var watcher: GooglePayAvailabilityUseCaseImpl

    override fun setup() {
        super.setup()

        whenever(activity.lifecycle).thenReturn(lifecycle)

        watcher = GooglePayAvailabilityUseCaseImpl(
            activity,
            cartRepository,
            isReadyToPayRequestUseCase,
            sessionFlags,
            setOf(featureToggles)
        )
    }

    /*
    isGooglePayAvailable
     */
    @Test
    fun `isGooglePayAvailable returns false if session flag was never set`() {
        returnNullForSessionFlag()

        assertFalse(watcher.isGooglePayAvailable())
    }

    @Test
    fun `isGooglePayAvailable returns value from session flag`() {
        arrayOf(true, false).forEach { value ->
            mockSessionFlag(value)

            assertEquals(value, watcher.isGooglePayAvailable())
        }
    }

    /*
    onDestroy
     */
    @Test
    fun `onDestroy doesn't crash if disposable is null`() {
        assertNull(watcher.disposable)

        watcher.onDestroy(mock())
    }

    @Test
    fun `onDestroy disposes disposable`() {
        watcher.disposable = mock()

        watcher.onDestroy(mock())

        verify(watcher.disposable!!).dispose()
    }

    /*
    startWatch
     */
    @Test
    fun `startWatch registers watcher as lifecycle observer`() {
        prepareStartWatch()

        watcher.startWatch()

        verify(lifecycle).addObserver(watcher)
    }

    @Test
    fun `startWatch stores disposable`() {
        prepareStartWatch()

        assertNull(watcher.disposable)

        watcher.startWatch()

        assertNotNull(watcher.disposable)
    }

    @Test
    fun `startWatch observes cart products on first invocation startWatch and on availability session flag not set`() {
        val cartProductsProcessor = prepareStartWatch()

        assertFalse(cartProductsProcessor.hasSubscribers())

        watcher.startWatch()

        assertTrue(cartProductsProcessor.hasSubscribers())
    }

    @Test
    fun `startWatch only checks google pay availability after cart has 1 product`() {
        val cartProductsProcessor = prepareStartWatch()

        val isReadyToPayRequestSubject = prepareIsReadyToPayRequest()

        watcher.startWatch()

        assertTrue(cartProductsProcessor.hasSubscribers())
        assertFalse(isReadyToPayRequestSubject.hasObservers())

        cartProductsProcessor.onNext(0)
        assertFalse(isReadyToPayRequestSubject.hasObservers())

        Timber.d("Pre emit")
        cartProductsProcessor.onNext(1)

        assertTrue(isReadyToPayRequestSubject.hasObservers())
    }

    @Test
    fun `startWatch stores session flag after isReadyToPayUseCase emits a value`() {
        val cartProductsProcessor = prepareStartWatch()

        val isReadyToPayRequestSubject = prepareIsReadyToPayRequest()

        assertFalse(cartProductsProcessor.hasSubscribers())

        watcher.startWatch()

        cartProductsProcessor.onNext(1)

        verify(sessionFlags, never()).setSessionFlag(eq(IS_GPAY_AVAILABLE_KEY), any())

        val expectedValue = true
        isReadyToPayRequestSubject.onSuccess(expectedValue)

        verify(sessionFlags).setSessionFlag(IS_GPAY_AVAILABLE_KEY, expectedValue)
    }

    @Test
    fun `startWatch checks google pay availability once, even if flowable emits multiple items`() {
        val cartProductsProcessor = prepareStartWatch()

        val isReadyToPayRequestSubject = prepareIsReadyToPayRequest()

        assertFalse(cartProductsProcessor.hasSubscribers())

        watcher.startWatch()

        assertTrue(cartProductsProcessor.hasSubscribers())
        assertFalse(isReadyToPayRequestSubject.hasObservers())

        verify(isReadyToPayRequestUseCase, never()).isReadyToPayRequest()

        cartProductsProcessor.onNext(4)

        verify(isReadyToPayRequestUseCase, times(1)).isReadyToPayRequest()

        cartProductsProcessor.onNext(2)

        verify(isReadyToPayRequestUseCase, times(1)).isReadyToPayRequest()
    }

    @Test
    fun `startWatch never observes cart products if session flag is set to false`() {
        val cartProductsProcessor = prepareStartWatch()

        mockSessionFlag(false)

        watcher.startWatch()

        assertFalse(cartProductsProcessor.hasSubscribers())
    }

    @Test
    fun `startWatch never observes cart products if session flag is set to true`() {
        val cartProductsProcessor = prepareStartWatch()

        mockSessionFlag(false)

        watcher.startWatch()

        assertFalse(cartProductsProcessor.hasSubscribers())
    }

    @Test
    fun `startWatch never observes cart products on second invocation`() {
        prepareStartWatch()

        watcher.startWatch()

        verify(cartRepository, times(1)).getCartProductsCount()

        watcher.startWatch()

        verify(cartRepository, times(1)).getCartProductsCount()
    }

    @Test
    fun `googlePay should be activated if toggle feature is turned on and session flags returns true`() {
        featureToggles.value = true
        whenever(sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY)).thenReturn(true)

        assertTrue(watcher.isGooglePayAvailable())
    }

    @Test
    fun `googlepay should be deactivated if toggle feature is turned off and session flags returns true`() {
        featureToggles.value = false
        whenever(sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY)).thenReturn(true)

        assertFalse(watcher.isGooglePayAvailable())
    }

    @Test
    fun `googlepay should be deactivated if toggle feature is turned on and session flags returns false`() {
        featureToggles.value = true
        whenever(sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY)).thenReturn(false)

        assertFalse(watcher.isGooglePayAvailable())
    }

    @Test
    fun `when googlepay is deactivated no interaction should be made with the components`() {
        featureToggles.value = false
        whenever(sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY)).thenReturn(true)

        watcher.startWatch()

        verifyZeroInteractions(lifecycle, cartRepository, isReadyToPayRequestUseCase, sessionFlags)
    }

    /*
    Utils
     */
    private fun prepareStartWatch(): PublishProcessor<Int> {
        val processor = PublishProcessor.create<Int>()
        whenever(cartRepository.getCartProductsCount()).thenReturn(processor)

        returnNullForSessionFlag()

        return processor
    }

    private fun prepareIsReadyToPayRequest(): SingleSubject<Boolean> {
        val subject = SingleSubject.create<Boolean>()
        whenever(isReadyToPayRequestUseCase.isReadyToPayRequest()).thenReturn(subject)

        return subject
    }

    private fun returnNullForSessionFlag() {
        mockSessionFlag(null)
    }

    private fun mockSessionFlag(value: Boolean?) {
        whenever(sessionFlags.readSessionFlag(IS_GPAY_AVAILABLE_KEY))
            .thenReturn(value)
    }
}

private const val IS_GPAY_AVAILABLE_KEY = "is_gpay_available"
