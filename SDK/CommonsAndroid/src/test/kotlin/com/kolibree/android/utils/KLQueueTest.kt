/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import com.google.common.base.Optional
import com.kolibree.android.failearly.FailEarly
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

internal class KLQueueTest {
    class FailEarlyException : Exception()

    private lateinit var queue: KLQueueImpl

    private val scheduler = TestScheduler()

    @Before
    fun setUp() {
        FailEarly.overrideDelegateWith { _, _ ->
            throw FailEarlyException()
        }

        queue = KLQueueImpl(
            delayScheduler = scheduler,
            delayAfterConsumption = Duration.ofSeconds(DELAY_AFTER_CONSUMPTION_SECONDS)
        )
    }

    @Test
    fun `when new item arrive on an empty queue it emit after the delay`() {
        val testObserver = queue.stream().test()
        queue.submit(lowItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), lowItem.optional())
    }

    @Test
    fun `when we subscribe to the stream we get the highest priority item`() {
        queue.submit(lowItem)
        queue.submit(highItem)
        queue.submit(mediumItem)

        val testObserver = queue.stream().test()

        testObserver.assertValue(highItem.optional())
    }

    @Test
    fun `when an item arrives before the delay it takes the one with higher priority`() {
        val testObserver = queue.stream().test()

        queue.submit(lowItem)
        queue.submit(highItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), highItem.optional())
    }

    @Test
    fun `when we consume an item it waits a delay before sending the next one even with higher priority`() {
        val testObserver = queue.stream().test()

        queue.submit(lowItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        queue.submit(highItem)

        testObserver.assertValues(Optional.absent(), lowItem.optional())

        queue.consume(lowItem)

        testObserver.assertValues(Optional.absent(), lowItem.optional())

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), lowItem.optional(), highItem.optional())
    }

    @Test
    fun `when there is no more item to consume it emits absent`() {
        val testObserver = queue.stream().test()

        queue.submit(lowItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        queue.consume(lowItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), lowItem.optional(), Optional.absent())
    }

    @Test
    fun `consume an item that is not yet popped from the queue have no effect`() {
        val testObserver = queue.stream().test()
        queue.submit(lowItem)
        queue.submit(highItem)
        queue.submit(mediumItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), highItem.optional())

        queue.consume(mediumItem)
        queue.consume(highItem)

        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(Optional.absent(), highItem.optional(), lowItem.optional())
    }

    @Test
    fun `consuming an item and submitting it back right after doesn't cause limbo`() {
        val testObserver = queue.stream().test()

        // first submit, which is received
        queue.submit(lowItem)
        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        // rapid consume/submit - cause of limbo
        queue.consume(lowItem)
        queue.submit(lowItem)
        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        // queue in limbo couldn't deliver those items
        queue.consume(lowItem)
        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        queue.submit(lowItem)
        scheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_SECONDS, TimeUnit.SECONDS)

        testObserver.assertValues(
            Optional.absent(),
            lowItem.optional(),
            Optional.absent(),
            lowItem.optional()
        )
    }

    companion object {
        val lowItem: KLItem = object : KLItem {
            override val priority: Priority = Priority.LOW
        }

        val mediumItem: KLItem = object : KLItem {
            override val priority: Priority = Priority.MEDIUM
        }

        val highItem: KLItem = object : KLItem {
            override val priority: Priority = Priority.HIGH
        }

        val urgentItem: KLItem = object : KLItem {
            override val priority: Priority = Priority.URGENT
        }

        fun KLItem.optional(): Optional<KLItem> = Optional.of(this)
    }
}

private const val DELAY_AFTER_CONSUMPTION_SECONDS = 1L
