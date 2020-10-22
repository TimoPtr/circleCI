/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.extensions.assertLastValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

internal class OfflineBrushingsRetrieverUseCaseTest : BaseUnitTest() {

    private val extractOfflineBrushingsUseCase = mock<ExtractOfflineBrushingsUseCase>()
    private val retrieverTrigger = mock<OfflineBrushingsRetrieverTrigger>()

    private lateinit var useCase: OfflineBrushingsRetrieverUseCase

    @Test
    fun `multiple invocations return same instance`() {
        whenever(retrieverTrigger.trigger).thenReturn(Flowable.empty())
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(Observable.empty())

        useCase = OfflineBrushingsRetrieverUseCaseImpl(
            extractOfflineBrushingsUseCase,
            retrieverTrigger
        )

        assertEquals(
            useCase.stream(),
            useCase.stream()
        )
    }

    @Test
    fun `multiple invocations return different instance if the first one disposed the stream`() {
        whenever(retrieverTrigger.trigger).thenReturn(Flowable.empty())
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(Observable.empty())

        useCase = OfflineBrushingsRetrieverUseCaseImpl(
            extractOfflineBrushingsUseCase,
            retrieverTrigger
        )

        val firstStream = useCase.stream()

            firstStream.test().dispose()

        assertNotSame(firstStream, useCase.stream())
    }

    @Test
    fun `retrievesOfflineBrushings emits ExtractionProgress when onConnectionsUpdatedStream emits`() {
        whenever(retrieverTrigger.trigger).thenReturn(Flowable.just(Unit))
        useCase = OfflineBrushingsRetrieverUseCaseImpl(extractOfflineBrushingsUseCase, retrieverTrigger)

        val expectedExtractionProgress = mock<ExtractionProgress>()

        whenever(retrieverTrigger.trigger).thenReturn(Flowable.just(Unit))

        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(Observable.just(expectedExtractionProgress))

        useCase.stream().test().assertLastValue(expectedExtractionProgress)
    }

    @Test
    fun `retrieving data starts with empty extraction progress`() {
        whenever(retrieverTrigger.trigger).thenReturn(Flowable.empty())
        useCase = OfflineBrushingsRetrieverUseCaseImpl(extractOfflineBrushingsUseCase, retrieverTrigger)

        whenever(retrieverTrigger.trigger).thenReturn(Flowable.empty())
        whenever(extractOfflineBrushingsUseCase.extractOfflineBrushings()).thenReturn(Observable.empty())

        val emptyProgress = ExtractionProgress.empty()
        useCase.stream().test().assertValue(emptyProgress)
    }
}
