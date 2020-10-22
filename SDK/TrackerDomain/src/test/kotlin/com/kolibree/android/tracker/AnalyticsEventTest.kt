/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.tracker

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotSame
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AnalyticsEventTest : BaseUnitTest() {

    @Test
    fun `details are null by default`() {
        val event = AnalyticsEvent("MyExtraFeature")
        assertNull(event.details)
    }

    @Test
    fun `name is preserved as-is when parent is null`() {
        assertEquals("MyExtraFeature", AnalyticsEvent("MyExtraFeature").name)
    }

    @Test
    fun `events are equal if they the same name`() {
        val event1 = AnalyticsEvent("MyExtraFeature")
        val event2 = AnalyticsEvent("MyExtraFeature")

        assertEquals(event1, event2)
        assertTrue(event1 == event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `events are equal if they the same name and details`() {
        val details = mapOf("key" to "value")
        val event1 = AnalyticsEvent("MyExtraFeature", details = details)
        val event2 = AnalyticsEvent("MyExtraFeature", details = details)

        assertEquals(event1, event2)
        assertTrue(event1 == event2)
        assertEquals(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `events are not equal if they have different names`() {
        val event1 = AnalyticsEvent("MyExtraFeature1")
        val event2 = AnalyticsEvent("MyExtraFeature2")

        assertNotSame(event1, event2)
        assertFalse(event1 == event2)
        assertNotSame(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `events are not equal if they have different names and the same details`() {
        val details = mapOf("key" to "value")
        val event1 = AnalyticsEvent("MyExtraFeature1", details = details)
        val event2 = AnalyticsEvent("MyExtraFeature2", details = details)

        assertNotSame(event1, event2)
        assertFalse(event1 == event2)
        assertNotSame(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `events are not equal if they have the same name but different details`() {
        val name = "MyExtraFeature"
        val event1 = AnalyticsEvent(name, details = mapOf("key" to "value"))
        val event2 = AnalyticsEvent(name, details = mapOf("key" to "other-value"))

        assertNotSame(event1, event2)
        assertFalse(event1 == event2)
        assertNotSame(event1.hashCode(), event2.hashCode())
    }

    @Test
    fun `name contains concatenated parent name if parent was passed`() {
        val parent = AnalyticsEvent("ParentFeature")
        assertEquals(
            "ParentFeature_MyExtraFeature",
            AnalyticsEvent("MyExtraFeature", parent = parent).name
        )
    }

    @Test
    fun `name contains concatenated parent name we add up multiple events`() {
        assertEquals(
            "ParentFeature_MyExtraFeature",
            (AnalyticsEvent("ParentFeature") + AnalyticsEvent("MyExtraFeature")).name
        )
    }

    @Test
    fun `name contains concatenated parent name we add up string to event`() {
        assertEquals(
            "ParentFeature_MyExtraFeature",
            (AnalyticsEvent("ParentFeature") + "MyExtraFeature").name
        )
    }

    @Test
    fun `parent inheritance chain is preserved in name`() {
        assertEquals(
            "1_2_3",
            AnalyticsEvent("3", parent = AnalyticsEvent("2", parent = AnalyticsEvent("1"))).name
        )
    }

    @Test
    fun `parent inheritance chain is when we add up multiple events`() {
        assertEquals(
            "1_2_3",
            (AnalyticsEvent("1") + AnalyticsEvent("2") + AnalyticsEvent("3")).name
        )
    }

    @Test
    fun `parent inheritance chain is when we add up event and multiple strings`() {
        assertEquals(
            "1_2_3",
            (AnalyticsEvent("1") + "2" + "3").name
        )
    }

    @Test
    fun `details are null when both events have null details`() {
        assertNull(
            AnalyticsEvent(
                "event",
                details = null,
                parent = AnalyticsEvent("parent", details = null)
            ).details
        )
    }

    @Test
    fun `details are null when added events have null details`() {
        assertNull(
            (AnalyticsEvent("parent", details = null) +
                AnalyticsEvent("event", details = null)).details
        )
    }

    @Test
    fun `details are null when parent event had null details before adding`() {
        assertNull(
            (AnalyticsEvent("parent", details = null) + "event").details
        )
    }

    @Test
    fun `details contains parent data if child has null details`() {
        val parentDetails = mapOf("key" to "value")

        val event = AnalyticsEvent(
            "event",
            details = null,
            parent = AnalyticsEvent("parent", details = parentDetails)
        )

        assertEquals(parentDetails, event.details)
    }

    @Test
    fun `details contains parent data after adding a string`() {
        val parentDetails = mapOf("key" to "value")

        val event = AnalyticsEvent("parent", details = parentDetails) + "event"

        assertEquals(parentDetails, event.details)
    }

    @Test
    fun `details contains child data if parent has null details`() {
        val details = mapOf("key" to "value")

        val event = AnalyticsEvent(
            "event",
            details = details,
            parent = AnalyticsEvent("parent", details = null)
        )

        assertEquals(details, event.details)
    }

    @Test
    fun `details contains child data if parent has null details after adding both together`() {
        val details = mapOf("key" to "value")

        val event = AnalyticsEvent("parent", details = null) +
            AnalyticsEvent("event", details = details)

        assertEquals(details, event.details)
    }

    @Test
    fun `details contains combined details if both events had some`() {
        val details = mapOf("key" to "value")
        val parentDetails = mapOf("other key" to "other value")

        val event = AnalyticsEvent(
            "event",
            details = details,
            parent = AnalyticsEvent("parent", details = parentDetails)
        )

        assertEquals(parentDetails + details, event.details)
    }

    @Test
    fun `details contains combined details if both added events had some`() {
        val details = mapOf("key" to "value")
        val parentDetails = mapOf("other key" to "other value")

        val event = AnalyticsEvent("parent", details = parentDetails) +
            AnalyticsEvent("event", details = details)

        assertEquals(parentDetails + details, event.details)
    }

    @Test
    fun `single detail adding works for nullable source`() {
        val newDetail = "other key" to "other value"

        val event = AnalyticsEvent("event") + newDetail

        assertEquals(mapOf(newDetail), event.details)
    }

    @Test
    fun `single detail adding combines details`() {
        val details = mapOf("key" to "value")
        val newDetail = "other key" to "other value"

        val event = AnalyticsEvent("event", details = details) + newDetail

        assertEquals(details + mapOf(newDetail), event.details)
    }

    @Test
    fun `details takes right-hand side precedence for values`() {
        val parentDetails = mapOf("key" to "original value")
        val details = mapOf("key" to "overridden value")

        val event = AnalyticsEvent(
            "event",
            details = details,
            parent = AnalyticsEvent("parent", details = parentDetails)
        )

        assertEquals(details, event.details)
    }

    @Test
    fun `details takes right-hand side precedence for values for adding`() {
        val parentDetails = mapOf("key" to "original value")
        val details = mapOf("key" to "overridden value")

        val event = AnalyticsEvent("parent", details = parentDetails) +
            AnalyticsEvent("event", details = details)

        assertEquals(details, event.details)
    }

    @Test
    fun `single detail adding  takes right-hand side precedence for values`() {
        val details = mapOf("key" to "original value")
        val newDetail = "key" to "overridden value"

        val event = AnalyticsEvent("event", details = details) + newDetail

        assertEquals(mapOf(newDetail), event.details)
    }

    @Test
    fun `details support nullification of values`() {
        val parentDetails = mapOf("key" to "original value")
        val details = mapOf("key" to null)

        val event = AnalyticsEvent(
            "event",
            details = details,
            parent = AnalyticsEvent("parent", details = parentDetails)
        )

        assertEquals(details, event.details)
    }

    @Test
    fun `details support nullification of values during adding`() {
        val parentDetails = mapOf("key" to "original value")
        val details = mapOf("key" to null)

        val event = AnalyticsEvent("parent", details = parentDetails) +
            AnalyticsEvent("event", details = details)

        assertEquals(details, event.details)
    }

    @Test
    fun `single detail addition supports nullification of values`() {
        val details = mapOf("key" to "original value")
        val newDetail = "key" to null

        val event = AnalyticsEvent("event", details = details) + newDetail

        assertEquals(mapOf(newDetail), event.details)
    }
}
