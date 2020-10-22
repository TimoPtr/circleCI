/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.collection

import java.lang.ref.WeakReference
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniquePoolTest {

    var uniquePool = UniquePool<String>()

    @Test
    fun `has correct parameters after creation`() {
        assertEquals(0, uniquePool.size())
        assertTrue(uniquePool.isEmpty)
        assertFalse(uniquePool.internal.iterator().hasNext())

        assertEquals(0, uniquePool.internal.size)
        assertTrue(uniquePool.internal.isEmpty())
        assertFalse(uniquePool.internal.iterator().hasNext())
    }

    @Test
    fun `can add single unique object to empty instance`() {
        val size = uniquePool.add("simple object")

        assertEquals(1, size)
        assertEquals(1, uniquePool.size())
        assertFalse(uniquePool.isEmpty)
        assertTrue(uniquePool.internal.iterator().hasNext())
        assertEquals("simple object", uniquePool.internal.iterator().next().get())
    }

    @Test
    fun `can add multiple unique objects to empty instance`() {
        val otherPool = UniquePool<String>().also {
            it.add("first")
            it.add("second")
            it.add("third")
        }

        val size = uniquePool.addAll(otherPool)

        assertEquals(3, size)
        assertEquals(3, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("second", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `can remove single unique object from instance`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        val size = uniquePool.remove("second")

        assertEquals(2, size)
        assertEquals(2, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `can remove multiple unique objects from instance`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        val size = uniquePool.removeAll(UniquePool<String>().also {
            it.add("second")
            it.add("third")
        })

        assertEquals(1, size)
        assertEquals(1, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `single non-unique object will be purged when added`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        val size = uniquePool.add("second")

        assertEquals(3, size)
        assertEquals(3, uniquePool.size())
    }

    @Test
    fun `multiple non-unique objects will be purged when added`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
            add("fourth")
        }

        val otherPool = UniquePool<String>().also {
            it.add("different first")
            it.add("second")
            it.add("different third")
            it.add("fourth")
        }

        uniquePool.addAll(otherPool)

        assertEquals(6, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("second", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertEquals("fourth", iterator.next().get())
        assertEquals("different first", iterator.next().get())
        assertEquals("different third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `object without reference will be purged when removed`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        uniquePool.remove("fourth")

        assertEquals(3, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("second", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `multiple non-unique objects will be purged when removed`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        uniquePool.removeAll(UniquePool<String>().also {
            it.add("second")
            it.add("third-and-a-half")
            it.add("fourth")
        })

        assertEquals(2, uniquePool.size())
        val iterator = uniquePool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `purge leaked objects while adding new ones`() {
        val evenWeakerPool = UniquePool<String?>().also {
            it.add("first")
            it.add("second")
        }

        // Simulate that object was purged by GC
        evenWeakerPool.internal[1] = WeakReference(null)

        evenWeakerPool.addAll(UniquePool<String?>().also {
            it.add(null)
            it.add("third")
        })

        assertEquals(2, evenWeakerPool.size())
        val iterator = evenWeakerPool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertEquals("third", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `purge leaked objects while removing others`() {
        val evenWeakerPool = UniquePool<String?>().also {
            it.add("first")
            it.add("second")
            it.add("third")
        }

        // Simulate that object was purged by GC
        evenWeakerPool.internal[1] = WeakReference(null)

        evenWeakerPool.removeAll(UniquePool<String?>().also {
            it.add("third")
            it.add(null)
        })

        assertEquals(1, evenWeakerPool.size())
        val iterator = evenWeakerPool.internal.iterator()
        assertEquals("first", iterator.next().get())
        assertFalse(iterator.hasNext())
    }

    @Test
    fun `clear removes all objects`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        uniquePool.clear()

        assertEquals(0, uniquePool.size())
        assertTrue(uniquePool.isEmpty)
    }

    @Test
    fun `forEach is invoked on every element`() {
        with(uniquePool) {
            add("first")
            add("second")
            add("third")
        }

        val list = arrayOfNulls<String>(3)
        var count = 0

        uniquePool.forEach { element -> list[count++] = element }

        assertEquals(3, count)
        assertArrayEquals(arrayOf("first", "second", "third"), list)
    }

    @Test
    fun `forEach purges null elements`() {
        val evenWeakerPool = UniquePool<String?>().also {
            it.add("first")
            it.add("second")
            it.add("third")
        }

        // Simulate that object was purged by GC
        evenWeakerPool.internal[1] = WeakReference(null)

        val list = arrayOfNulls<String>(2)
        var count = 0

        evenWeakerPool.forEach { element -> list[count++] = element }

        assertEquals(2, evenWeakerPool.size())
        assertEquals(2, count)
        assertArrayEquals(arrayOf("first", "third"), list)
    }
}
