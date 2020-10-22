/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.mouthmap.logic.BrushingResultsMapper
import com.kolibree.android.processedbrushings.CheckupData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TestBrushingResultsProviderImplTest : BaseUnitTest() {

    private val converter = mock<BrushingResultsMapper>()

    internal lateinit var resultsProvider: TestBrushingResultsProviderImpl

    override fun setup() {
        super.setup()

        resultsProvider = spy(
            TestBrushingResultsProviderImpl(
                converter
            )
        )
    }

    @Test
    fun `init invokes converter's convert method`() {
        val checkupData = mock<CheckupData>()
        val result = BrushingResults()
        whenever(converter.map(checkupData)).thenReturn(result)

        resultsProvider.init(checkupData)

        verify(converter).map(checkupData)
    }

    @Test
    fun `init assigne converter's BrushingResult object to results variable`() {
        val checkupData = mock<CheckupData>()
        val brushingResults = BrushingResults(
            coverage = 34,
            duration = 123
        )
        whenever(converter.map(checkupData)).thenReturn(brushingResults)

        resultsProvider.results = BrushingResults()
        resultsProvider.init(checkupData)

        assertEquals(brushingResults, resultsProvider.results)
    }

    @Test
    fun `provide returns instance results variable`() {
        val brushingResults = BrushingResults(
            coverage = 34,
            duration = 123
        )
        resultsProvider.results = brushingResults

        assertEquals(brushingResults, resultsProvider.provide())
    }
}
