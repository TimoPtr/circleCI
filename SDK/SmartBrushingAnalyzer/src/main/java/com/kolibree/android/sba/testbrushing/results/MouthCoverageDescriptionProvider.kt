package com.kolibree.android.sba.testbrushing.results

import android.content.Context
import androidx.annotation.StringRes
import com.kolibree.android.mouthmap.logic.MouthCoverageResult
import com.kolibree.android.sba.R

internal class MouthCoverageDescriptionProvider(val context: Context) : DescriptionProvider<MouthCoverageResult> {

    override fun description(result: MouthCoverageResult): String {
        val stringRes = mouthCoverageCardDescription(result.coverage)
        return context.getString(stringRes)
    }

    @StringRes
    fun mouthCoverageCardDescription(coverage: Int): Int = when {
        coverage == MOUTH_COVERAGE_PERFECT -> R.string.mouth_coverage_perfect
        coverage >= MOUTH_COVERAGE_GOOD -> R.string.mouth_coverage_good
        coverage >= MOUTH_COVERAGE_MEDIUM -> R.string.mouth_coverage_medium
        coverage > MOUTH_COVERAGE_NO_DATA -> R.string.mouth_coverage_bad
        else -> R.string.mouth_coverage_no_data
    }

    companion object {
        const val MOUTH_COVERAGE_PERFECT = 100
        const val MOUTH_COVERAGE_GOOD = 85
        const val MOUTH_COVERAGE_MEDIUM = 50
        const val MOUTH_COVERAGE_NO_DATA = 0
    }
}
