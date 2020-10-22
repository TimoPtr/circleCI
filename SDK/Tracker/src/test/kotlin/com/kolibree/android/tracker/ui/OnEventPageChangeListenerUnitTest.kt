/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.ui

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.tracker.AnalyticsEvent
import com.nhaarman.mockitokotlin2.any
import org.junit.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify

class OnEventPageChangeListenerUnitTest : BaseUnitTest() {

    @Test
    fun listenerCreation_doesntInvokeAnyEvent() {
        OnEventPageChangeListener(
            EVENT_1,
            EVENT_2
        )
        verify(eventTracker, never()).sendEvent(any())
    }

    @Test
    fun onPageSelected_invokesSendEventWithAppropriateEvent() {
        val listener = OnEventPageChangeListener(
            EVENT_1,
            EVENT_2,
            EVENT_3
        )
        listener.onPageSelected(1)
        verify(eventTracker).sendEvent(EVENT_2)
        listener.onPageSelected(2)
        verify(eventTracker).sendEvent(EVENT_3)
        listener.onPageSelected(0)
        verify(eventTracker).sendEvent(EVENT_1)
    }

    @Test
    fun onPageSelected_indexGreaterThanEvents() {
        val events = arrayOf(
            EVENT_1,
            EVENT_2
        )
        val listener = OnEventPageChangeListener(
            *events
        )
        reset(eventTracker)
        listener.onPageSelected(events.size)
        verify(eventTracker, never()).sendEvent(any())
    }

    companion object {

        private val EVENT_1 = AnalyticsEvent("Event1")
        private val EVENT_2 = AnalyticsEvent("Event2")
        private val EVENT_3 = AnalyticsEvent("Event3")
    }
}
