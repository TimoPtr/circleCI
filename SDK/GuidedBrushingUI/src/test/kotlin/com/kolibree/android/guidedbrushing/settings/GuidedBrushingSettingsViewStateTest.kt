/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.settings

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GuidedBrushingSettingsViewStateTest : BaseUnitTest() {

    @Test
    fun `withSettings updates viewState`() {
        val settings = CoachSettings.create(profileId = 123L)
            .updateEnableBrushingMovement(true)
            .updateEnableHelpText(true)
            .updateEnableTransitionSounds(false)
            .updateEnableMusic(true)
        val viewState = GuidedBrushingSettingsViewState().withSettings(settings)
        assertTrue(viewState.isDisplayBrushingMovementOn)
        assertTrue(viewState.isDisplayHelpTextsOn)
        assertFalse(viewState.isTransitionSoundsOn)
        assertTrue(viewState.isMusicOn)
    }
}
