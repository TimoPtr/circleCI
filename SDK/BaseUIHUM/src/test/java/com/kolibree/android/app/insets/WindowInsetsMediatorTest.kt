/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.insets

import android.view.View
import android.view.WindowInsets
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class WindowInsetsMediatorTest : BaseUnitTest() {

    @Test
    fun `capture window insets upon construction`() {
        val insets: WindowInsets = mock()
        val view: View = mock()

        whenever(view.setOnApplyWindowInsetsListener(any()))
            .thenAnswer {
                (it.arguments[0] as View.OnApplyWindowInsetsListener)
                    .onApplyWindowInsets(view, insets)
            }

        val mediator = WindowInsetsMediator(view)

        verify(view).setOnApplyWindowInsetsListener(any())
        assertEquals(insets, mediator.windowInsets)
    }

    @Test
    fun `add receiver to pending list if insets are not yet available`() {
        val insets: WindowInsets = mock()
        val view: View = mock()
        val receiver: (WindowInsets) -> Unit = mock()

        whenever(view.setOnApplyWindowInsetsListener(any()))
            .thenAnswer { /* no-op */ }

        val mediator = WindowInsetsMediator(view)

        mediator.withWindowInsets(receiver)

        verify(receiver, never()).invoke(insets)
        assertFalse(mediator.pendingList.isEmpty())
    }

    @Test
    fun `serve all pending receivers with insets when they're available and clear pending list`() {
        val delaySeconds = 1L
        val insets: WindowInsets = mock()
        val view: View = mock()
        val receiver: (WindowInsets) -> Unit = mock()

        val scheduler = TestScheduler()
        val longOpSimulator = Completable.timer(delaySeconds, TimeUnit.SECONDS, scheduler)

        whenever(view.setOnApplyWindowInsetsListener(any()))
            .thenAnswer { listener ->
                longOpSimulator.andThen {
                    (listener.arguments[0] as View.OnApplyWindowInsetsListener)
                        .onApplyWindowInsets(view, insets)
                }.subscribe()
            }

        val mediator = WindowInsetsMediator(view)

        mediator.withWindowInsets(receiver)

        verify(receiver, never()).invoke(insets)
        assertFalse(mediator.pendingList.isEmpty())

        scheduler.advanceTimeBy(delaySeconds, TimeUnit.SECONDS)

        verify(receiver).invoke(insets)
        assertTrue(mediator.pendingList.isEmpty())
    }

    @Test
    fun `serve receiver with insets if they're available, without adding it to pending list`() {
        val insets: WindowInsets = mock()
        val view: View = mock()
        val receiver: (WindowInsets) -> Unit = mock()

        whenever(view.setOnApplyWindowInsetsListener(any()))
            .thenAnswer {
                (it.arguments[0] as View.OnApplyWindowInsetsListener)
                    .onApplyWindowInsets(view, insets)
            }

        val mediator = WindowInsetsMediator(view)

        mediator.withWindowInsets(receiver)

        verify(receiver).invoke(insets)
        assertTrue(mediator.pendingList.isEmpty())
    }
}
