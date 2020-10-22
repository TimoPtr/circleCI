/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import io.reactivex.processors.PublishProcessor
import org.junit.Test

internal class BrushingModeChangedUseCaseTest : BaseUnitTest() {
    private val brushingModeChanged = BrushingModeChangedUseCase()

    @Test
    fun `stream emits payload if notification starts with BRUSHING_MODE_PARAMETER`() {
        val subject = PublishProcessor.create<ByteArray>()

        val observer = brushingModeChanged.brushingModeChangedStream(subject).test().assertEmpty()

        val expectedEvent = byteArrayOf(BRUSHING_MODE_PARAMETER)
        subject.onNext(expectedEvent)

        observer.assertValue(expectedEvent)
    }

    @Test
    fun `stream doesn't emit duplicated payloads`() {
        val subject = PublishProcessor.create<ByteArray>()

        val observer = brushingModeChanged.brushingModeChangedStream(subject).test().assertEmpty()

        val expectedEvent = byteArrayOf(BRUSHING_MODE_PARAMETER)
        subject.onNext(expectedEvent)
        subject.onNext(expectedEvent)

        observer.assertValueCount(1).assertValue(expectedEvent)
    }

    @Test
    fun `stream emits different payloads`() {
        val subject = PublishProcessor.create<ByteArray>()

        val observer = brushingModeChanged.brushingModeChangedStream(subject).test().assertEmpty()

        val firstEvent = byteArrayOf(BRUSHING_MODE_PARAMETER, 1)
        subject.onNext(firstEvent)

        observer.assertValues(firstEvent)

        val secondEvent = byteArrayOf(BRUSHING_MODE_PARAMETER, 2)
        subject.onNext(secondEvent)

        observer.assertValues(firstEvent, secondEvent)
    }

    @Test
    fun `stream never emits payload if notification doesn't start with BRUSHING_MODE_PARAMETER`() {
        val subject = PublishProcessor.create<ByteArray>()

        val observer = brushingModeChanged.brushingModeChangedStream(subject).test().assertEmpty()

        subject.onNext(byteArrayOf((BRUSHING_MODE_PARAMETER - 1).toByte()))
        subject.onNext(byteArrayOf((BRUSHING_MODE_PARAMETER + 1).toByte()))

        observer.assertEmpty()
    }

    @Test
    fun `stream doesn't crash if notification is empty`() {
        val subject = PublishProcessor.create<ByteArray>()

        brushingModeChanged.brushingModeChangedStream(subject).test().assertEmpty()

        subject.onNext(byteArrayOf())
    }
}
