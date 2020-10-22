/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.matcher

import android.view.View
import com.kolibree.android.app.ui.home.tab.view.CoverageDurationProgressView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

object CoverageDurationProgressViewMatchers {

    fun withCoverage(coverage: Float): TypeSafeMatcher<View> = CoverageDurationProgressViewMatcher(
        coverage = coverage,
        description = "with coverage"
    )

    fun withDuration(duration: Float): TypeSafeMatcher<View> = CoverageDurationProgressViewMatcher(
        duration = duration,
        description = "with duration"
    )

    private class CoverageDurationProgressViewMatcher(
        private val coverage: Float? = null,
        private val duration: Float? = null,
        private val description: String
    ) : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText(this.description)
        }

        override fun matchesSafely(item: View): Boolean {
            if (item !is CoverageDurationProgressView) {
                return false
            }

            return when {
                coverage != null -> coverage == item.getCoverage()
                duration != null -> duration == item.getDuration()
                else -> false
            }
        }
    }
}
