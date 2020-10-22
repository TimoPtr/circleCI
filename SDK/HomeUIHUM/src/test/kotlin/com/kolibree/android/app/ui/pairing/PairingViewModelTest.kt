/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.pairing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.test.invokeOnCleared
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_MAC
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.CompletableSubject
import io.reactivex.subjects.PublishSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class PairingViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: PairingViewModel
    private val blinkingConnectionHolder: BlinkingConnectionHolder = mock()

    override fun setup() {
        super.setup()

        viewModel = PairingViewModel(null, blinkingConnectionHolder)
    }

    /*
    onCleared
     */
    @Test
    fun `onCleared does nothing if blinkingConnection is null`() {
        assertNull(viewModel.blinkingConnection)

        viewModel.invokeOnCleared()
    }

    @Test
    fun `onCleared unpairs from blinkingConnection in a blocking fashion if it's not null`() {
        viewModel.blinkingConnection = KLTBConnectionBuilder.createAndroidLess().build()

        /*
        While this subject has observers, it means we didn't invoke parent.onCleared
         */
        val testSubject = PublishSubject.create<Boolean>()
        viewModel.disposeOnCleared {
            testSubject.subscribe()
        }

        val subject = CompletableSubject.create()
        doAnswer {
            subject
                .doOnSubscribe {
                    assertTrue(testSubject.hasObservers())

                    subject.onComplete()
                }
        }.whenever(blinkingConnectionHolder).unpairBlinkingConnectionCompletable()

        assertTrue(testSubject.hasObservers())

        viewModel.invokeOnCleared()

        assertFalse(testSubject.hasObservers())
    }

    @Test
    fun `onCleared invokes super oncleared even if unpair throws exception`() {
        viewModel.blinkingConnection = KLTBConnectionBuilder.createAndroidLess().build()

        /*
        While this subject has observers, it means we didn't invoke parent.onCleared
         */
        val testSubject = PublishSubject.create<Boolean>()
        viewModel.disposeOnCleared {
            testSubject.subscribe()
        }

        val subject = CompletableSubject.create()
        doAnswer {
            subject
                .doOnSubscribe {
                    assertTrue(testSubject.hasObservers())

                    subject.onError(TestForcedException())
                }
        }.whenever(blinkingConnectionHolder).unpairBlinkingConnectionCompletable()

        assertTrue(testSubject.hasObservers())

        viewModel.invokeOnCleared()

        assertFalse(testSubject.hasObservers())
    }

    /*
    unpairBlinkingConnectionCompletable
     */

    @Test
    fun `unpairBlinkingConnectionCompletable unpairs from blinkingConnection in a non-blocking fashion if it's not null`() {
        viewModel.blinkingConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val unpairSubject = mockUnpair(DEFAULT_MAC)

        viewModel.unpairBlinkingConnectionCompletable().test()

        assertTrue(unpairSubject.hasObservers())
    }

    /*
    Utils
     */
    private fun mockUnpair(mac: String): CompletableSubject {
        val subject = CompletableSubject.create()

        whenever(blinkingConnectionHolder.unpairBlinkingConnectionCompletable()).thenReturn(subject)

        return subject
    }
}
