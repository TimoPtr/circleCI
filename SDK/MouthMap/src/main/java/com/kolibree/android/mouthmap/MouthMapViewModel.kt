/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.mouthmap

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.commons.DurationFormatter
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.ShowPlaqlessVersionOfViewsFeature
import com.kolibree.android.feature.toggleForFeature
import com.kolibree.android.mouthmap.logic.BrushingResults
import javax.inject.Inject

@Keep
class MouthMapViewModel(
    private val brushingResults: BrushingResults,
    private val alwaysShowPlaqlessCheckupFeatureToggle: FeatureToggle<Boolean>
) : BaseViewModel<
    EmptyBaseViewState,
    MouthMapAction
    >(EmptyBaseViewState) {

    private val formatter = DurationFormatter()

    val showPlaqlessCheckup = brushingResults.hasPlaqlessData || alwaysShowPlaqlessVersion()

    val colorMouthZones = with(brushingResults) {
        if (showPlaqlessCheckup) plaqlessColorMouthZones else coverageColorMouthZones
    }

    val coverage = with(brushingResults) {
        if (showPlaqlessCheckup) cleanScore() else coverage
    }

    val hasCoverageData = coverage >= 0

    val duration = formatter.format(brushingResults.duration.toLong(), false)

    fun userClickCleanScore() = with(brushingResults) {
        pushAction(
            ShowCoverageDialog(
                showPlaqlessCheckup, buildUpRemains, missedAreas,
                this@MouthMapViewModel.coverage
            )
        )
    }

    fun userClickMissedDetails() = pushAction(ShowMissedAreaDialog)

    fun userClickRemainsDetails() {
        if (showPlaqlessCheckup) pushAction(ShowBuildUpRemainsDialog)
    }

    fun userClickJaws() = pushAction(ToggleJawsView)

    private fun alwaysShowPlaqlessVersion() = alwaysShowPlaqlessCheckupFeatureToggle.value

    @Suppress("UNCHECKED_CAST")
    class Factory @Inject constructor(
        private val brushingResults: BrushingResults,
        private val featureToggles: FeatureToggleSet
    ) : BaseViewModel.Factory<EmptyBaseViewState>() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            MouthMapViewModel(
                brushingResults,
                featureToggles.toggleForFeature(ShowPlaqlessVersionOfViewsFeature)
            ) as T
    }
}
