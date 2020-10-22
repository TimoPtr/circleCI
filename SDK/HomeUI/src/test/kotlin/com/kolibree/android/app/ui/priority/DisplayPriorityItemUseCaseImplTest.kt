/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.priority

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.utils.DELAY_AFTER_CONSUMPTION_MILLISECONDS
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.KLQueueFactory
import com.kolibree.android.utils.Priority
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test

class DisplayPriorityItemUseCaseImplTest : BaseUnitTest() {

    private lateinit var testScheduler: TestScheduler
    private lateinit var useCase: DisplayPriorityItemUseCase<KLItem>

    private val lowItem = klItem(Priority.LOW)
    private val mediumItem = klItem(Priority.MEDIUM)
    private val highItem = klItem(Priority.HIGH)
    private val urgentItem = klItem(Priority.URGENT)

    override fun setup() {
        super.setup()
        testScheduler = TestScheduler()
        val queue = KLQueueFactory.creates(delayScheduler = testScheduler)
        useCase = DisplayPriorityItemUseCaseImpl(priorityQueue = queue)
    }

    @Test
    fun `display item when there are no other items in the queue`() {
        val observer = useCase.submitAndWaitFor(lowItem).test()
        observer.assertComplete()
    }

    @Test
    fun `display only one item at the time`() {
        val urgentObserver = useCase.submitAndWaitFor(urgentItem).test()
        val highObserver = useCase.submitAndWaitFor(highItem).test()
        val mediumObserver = useCase.submitAndWaitFor(mediumItem).test()
        val lowObserver = useCase.submitAndWaitFor(lowItem).test()

        assertComplete(urgentObserver)
        assertNotComplete(highObserver, mediumObserver, lowObserver)

        useCase.markAsDisplayed(urgentItem)
        advanceTime()

        assertComplete(urgentObserver, highObserver)
        assertNotComplete(mediumObserver, lowObserver)

        useCase.markAsDisplayed(highItem)
        advanceTime()

        assertComplete(urgentObserver, highObserver, mediumObserver)
        assertNotComplete(lowObserver)

        useCase.markAsDisplayed(mediumItem)
        advanceTime()

        assertComplete(urgentObserver, highObserver, mediumObserver, lowObserver)
    }

    @Test
    fun `throws exception if item is not present in the queue`() {
        val urgentObserver = useCase.submitAndWaitFor(urgentItem).test()
        val highObserver = useCase.submitAndWaitFor(highItem).test()

        // mark item before it was displayed == it's not longer in the queue but use case is still waiting
        useCase.markAsDisplayed(highItem)
        useCase.markAsDisplayed(urgentItem)
        advanceTime()

        urgentObserver.assertComplete()
        highObserver.assertError(IllegalStateException::class.java)
    }

    private fun assertComplete(vararg observers: TestObserver<*>) {
        observers.forEach { it.assertComplete() }
    }

    private fun assertNotComplete(vararg observers: TestObserver<*>) {
        observers.forEach { it.assertNotComplete() }
    }

    private fun advanceTime() {
        testScheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_MILLISECONDS, TimeUnit.MILLISECONDS)
    }

    private fun klItem(priority: Priority) = object : KLItem {
        override val priority = priority
    }
}
