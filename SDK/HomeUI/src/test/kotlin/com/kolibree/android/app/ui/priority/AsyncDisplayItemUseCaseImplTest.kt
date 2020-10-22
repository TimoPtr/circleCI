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
import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit
import org.junit.Test

class AsyncDisplayItemUseCaseImplTest : BaseUnitTest() {

    private lateinit var testScheduler: TestScheduler
    private lateinit var useCase: AsyncDisplayItemUseCase<KLItem>

    private val lowItem = TestItem(Priority.LOW)
    private val mediumItem = TestItem(Priority.MEDIUM)
    private val highItem = TestItem(Priority.HIGH)
    private val urgentItem = TestItem(Priority.URGENT)

    private val differentLowItem = DifferentTestItem(Priority.LOW)
    private val differentUrgentItem = DifferentTestItem(Priority.URGENT)

    override fun setup() {
        super.setup()
        testScheduler = TestScheduler()
        val queue = KLQueueFactory.creates(delayScheduler = testScheduler)
        useCase = AsyncDisplayItemUseCaseImpl(priorityQueue = queue)
    }

    @Test
    fun `submit item and complete immediately`() {
        val observer = useCase.submit(lowItem).test()
        observer.assertComplete()
    }

    @Test
    fun `submit multiple items as non-blocking operation`() {
        useCase.submit(lowItem).test().assertComplete()
        useCase.submit(urgentItem).test().assertComplete()
        useCase.submit(highItem).test().assertComplete()
        useCase.submit(mediumItem).test().assertComplete()
    }

    @Test
    fun `submitted item is available for consumption if we listen for type`() {
        val observer = useCase.listenFor(TestItem::class).test()
        observer.assertNoValues()

        useCase.submit(lowItem).test().assertComplete()
        advanceTime()

        observer.assertValue(lowItem)

        observer.assertNotComplete()
    }

    @Test
    fun `submitted item is available for consumption if we listen for item`() {
        val observer = useCase.waitFor(lowItem).test()
        observer.assertNoValues()

        useCase.submit(lowItem).test().assertComplete()
        advanceTime()

        observer.assertComplete()
    }

    @Test
    fun `priority of items is preserved if we wait for particular type`() {
        val observer = useCase.listenFor(TestItem::class).test()
        observer.assertNoValues()

        useCase.submit(lowItem).test().assertComplete()
        useCase.submit(urgentItem).test().assertComplete()
        useCase.submit(mediumItem).test().assertComplete()
        useCase.submit(highItem).test().assertComplete()

        advanceTime()
        observer.assertValues(urgentItem)

        useCase.markAsDisplayed(urgentItem)
        advanceTime()
        observer.assertValues(urgentItem, highItem)

        useCase.markAsDisplayed(highItem)
        advanceTime()
        observer.assertValues(urgentItem, highItem, mediumItem)

        useCase.markAsDisplayed(mediumItem)
        advanceTime()
        observer.assertValues(urgentItem, highItem, mediumItem, lowItem)

        observer.assertNotComplete()
    }

    @Test
    fun `get only item of types you wait for`() {
        val testObserver = useCase.listenFor(TestItem::class).test()
        val differentTestObserver = useCase.listenFor(DifferentTestItem::class).test()

        testObserver.assertNoValues()
        differentTestObserver.assertNoValues()

        useCase.submit(lowItem).test().assertComplete()
        advanceTime()
        testObserver.assertValues(lowItem)
        differentTestObserver.assertNoValues()

        useCase.submit(differentLowItem).test().assertComplete()
        useCase.markAsDisplayed(lowItem)
        advanceTime()
        testObserver.assertValues(lowItem)
        differentTestObserver.assertValues(differentLowItem)

        useCase.submit(differentUrgentItem).test().assertComplete()
        useCase.markAsDisplayed(differentLowItem)
        advanceTime()
        testObserver.assertValues(lowItem)
        differentTestObserver.assertValues(differentLowItem, differentUrgentItem)

        useCase.submit(urgentItem).test().assertComplete()
        useCase.markAsDisplayed(differentUrgentItem)
        advanceTime()
        testObserver.assertValues(lowItem, urgentItem)
        differentTestObserver.assertValues(differentLowItem, differentUrgentItem)
    }

    private fun advanceTime() {
        testScheduler.advanceTimeBy(DELAY_AFTER_CONSUMPTION_MILLISECONDS, TimeUnit.MILLISECONDS)
    }

    private data class TestItem(override val priority: Priority) : KLItem

    private data class DifferentTestItem(override val priority: Priority) : KLItem
}
