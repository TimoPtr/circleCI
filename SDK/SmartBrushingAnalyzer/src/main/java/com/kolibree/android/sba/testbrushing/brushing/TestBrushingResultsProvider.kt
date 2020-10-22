/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sba.testbrushing.brushing

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.mouthmap.logic.BrushingResultsMapper
import com.kolibree.android.processedbrushings.CheckupData
import javax.inject.Inject

@Keep
interface TestBrushingResultsProvider {
    fun provide(): BrushingResults
    fun init(checkupData: CheckupData)
}

@Keep
class TestBrushingResultsProviderImpl @Inject constructor(private val mapper: BrushingResultsMapper) :
    TestBrushingResultsProvider {

    @VisibleForTesting
    lateinit var results: BrushingResults

    override fun provide() = results

    override fun init(checkupData: CheckupData) {
        results = mapper.map(checkupData)
    }
}
