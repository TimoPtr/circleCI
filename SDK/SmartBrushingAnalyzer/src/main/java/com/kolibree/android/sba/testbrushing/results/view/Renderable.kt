package com.kolibree.android.sba.testbrushing.results.view

import com.kolibree.android.mouthmap.logic.Result

internal interface Renderable<T : Result> : VisibilityChangeable {
    fun onRender(result: T) {
        // no-op
    }
}

internal interface VisibilityChangeable {
    fun onViewVisible() {
        // no-op
    }

    fun onViewInvisible() {
        // no-op
    }
}
