package com.kolibree.android.sba.testbrushing.results

import com.kolibree.android.mouthmap.logic.Result

internal interface DescriptionProvider<RESULT : Result> {
    fun description(result: RESULT): String
}
