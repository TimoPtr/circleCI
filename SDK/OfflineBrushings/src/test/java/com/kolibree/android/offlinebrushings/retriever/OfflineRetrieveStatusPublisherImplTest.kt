/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.retriever

import android.os.Handler
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.offlinebrushings.ExtractionProgress
import com.kolibree.android.test.extensions.assertLastValue
import com.kolibree.android.test.extensions.postImmediateRun
import com.kolibree.android.test.extensions.withFixedInstant
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class OfflineRetrieveStatusPublisherImplTest : BaseUnitTest() {
    private val handler: Handler = mock()

    private val publisher = OfflineRetrieveStatusPublisherImpl(handler)

    override fun setup() {
        super.setup()

        handler.postImmediateRun()
    }

    @Test
    fun `when subscribing to empty publisher, stream emits no items`() {
        publisher.stream().test().assertEmpty()
    }

    @Test
    fun `when subscribing to a publisher with one item, stream emits the last item`() =
        withFixedInstant {
            val extractionProgress = ExtractionProgress.empty()
            publisher.accept(extractionProgress)

            publisher.stream().test()
                .assertValues(extractionProgress.timestamped())
                .assertNotComplete()
        }

    @Test
    fun `when subscribing to a publisher with multiple items, stream emits the last item`() =
        withFixedInstant {
            val expectedItem = nonEmptyExtractionProgress(3)
            publisher.accept(ExtractionProgress.empty())
            publisher.accept(nonEmptyExtractionProgress(2))
            publisher.accept(expectedItem)

            publisher.stream().test()
                .assertValues(expectedItem.timestamped())
                .assertNotComplete()
        }

    @Test
    fun `when subscribing to a publisher and it emits multiple items, stream emits all of them`() =
        withFixedInstant {
            val observer = publisher.stream().test().assertEmpty()

            val firstItem = ExtractionProgress.empty()
            val secondItem = nonEmptyExtractionProgress(2)
            val thirdItem = nonEmptyExtractionProgress(3)
            publisher.accept(firstItem)

            observer
                .assertValues(firstItem.timestamped())
                .assertNotComplete()

            publisher.accept(secondItem)

            observer
                .assertValues(firstItem.timestamped(), secondItem.timestamped())
                .assertNotComplete()

            publisher.accept(thirdItem)

            observer
                .assertValues(
                    firstItem.timestamped(),
                    secondItem.timestamped(),
                    thirdItem.timestamped()
                )
                .assertNotComplete()
        }

    @Test
    fun `when subscribed to a publisher with a non-empty ExtractionProgress and observer flags item as consumed, stream emits an empty ExtractionProgress`() =
        withFixedInstant {
            val observer = publisher.stream().test().assertEmpty()

            val extractionProgress = nonEmptyExtractionProgress(totalBrushings = 2)
            publisher.accept(extractionProgress)

            observer
                .assertValueCount(1)
                .assertValues(extractionProgress.timestamped())
                .assertNotComplete()

            publisher.consume(extractionProgress.timestamped()).test()

            observer.assertValueCount(2)
                .assertLastValue(ExtractionProgress.empty().timestamped())
        }

    @Test
    fun `when subscribed to a publisher with an empty ExtractionProgress and observer flags item as consumed, stream does not emit a new value`() =
        withFixedInstant {
            val observer = publisher.stream().test().assertEmpty()

            val extractionProgress = ExtractionProgress.empty()
            publisher.accept(extractionProgress)

            observer
                .assertValueCount(1)
                .assertValues(extractionProgress.timestamped())
                .assertNotComplete()

            publisher.consume(extractionProgress.timestamped()).test()

            observer.assertValueCount(1)
        }

    @Test
    fun `when subscribing to a publisher with one item that was flagged as consumed, stream emits empty item`() =
        withFixedInstant {
            val extractionProgress = nonEmptyExtractionProgress(3)
            publisher.accept(extractionProgress)

            publisher.consume(extractionProgress.timestamped()).test()

            publisher.stream().test().assertValue(ExtractionProgress.empty().timestamped())
        }

    private fun nonEmptyExtractionProgress(totalBrushings: Int): ExtractionProgress {
        return ExtractionProgress.withBrushingProgress(
            brushingsSynced = emptyList(),
            totalBrushings = totalBrushings
        )
    }

    private fun ExtractionProgress.timestamped(): TimestampedExtractionProgress {
        return TimestampedExtractionProgress.fromExtractionProgress(this)
    }
}
