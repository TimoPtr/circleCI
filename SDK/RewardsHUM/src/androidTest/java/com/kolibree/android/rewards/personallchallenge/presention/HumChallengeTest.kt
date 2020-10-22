/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personallchallenge.presention

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.ui.text.highlightString
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.personalchallenge.model.HumChallengeInternal
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallengeInternalResources
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.OnGoingChallenge
import com.kolibree.android.test.BaseInstrumentationTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HumChallengeTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun titleIsValid() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.title
                    )
            ),
            challenge.title(context())
        )
    }

    @Test
    fun resourcesIsValid() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverOfflineBrushing)

        assertEquals(
            HumChallengeInternalResources.DiscoverOfflineBrushing, challenge.resources
        )
    }

    @Test
    fun subTitleIsValidAndHighlighted() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverOfflineBrushing)

        assertEquals(
            highlightString(
                context().getString(
                    HumChallengeInternalResources.DiscoverOfflineBrushing.subTitle
                ),
                context().getString(
                    HumChallengeInternalResources.DiscoverOfflineBrushing.subTitleHighlight
                )
            ).toString(),
            challenge.subTitle(context()).toString()
        )
    }

    @Test
    fun actionMapActionFromRecommendationRes() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            HumChallengeInternalResources.DiscoverGuidedBrushing.action,
            challenge.action
        )
    }

    @Test
    fun smilesMapSmilesFromRecommendation() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(HumChallengeInternal.DiscoverGuidedBrushing.smiles, challenge.smiles)
    }

    @Test
    fun isMoreThanOneDayReturnsDependingOnTheDuration() {
        assertFalse(NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing).isMoreThanOneDay())
        assertTrue(NotAcceptedChallenge(HumChallengeInternal.BrushFor5Days).isMoreThanOneDay())
    }

    /*
    NotAcceptedChallenge
     */
    @Test
    fun notAcceptedChallengeHave0AsProgress() {
        assertEquals(
            0,
            NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing).progress
        )
    }

    @Test
    fun notAcceptedChallengeUseTitleDescriptionNotAcceptedAsTitleDescription() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.titleDescriptionNotAccepted
                    )
            ),
            challenge.titleDescription(context())
        )
    }

    @Test
    fun notAcceptedChallengeUseDescriptionNotAcceptedAsDescriptionWithHighlight() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            highlightString(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionNotAccepted
                    ),
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionHighlightNotAccepted
                    )
            ).toString(),
            challenge.description(context()).toString()
        )
    }

    @Test
    fun notAcceptedChallengeUseAcceptChallengeAsActionText() {
        val challenge = NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.acceptChallengeText
                    )
            ),
            challenge.actionText(context())
        )
    }

    /*
    OnGoingChallenge
    */

    @Test
    fun onGoingChallengeHaveMatchProgress() {
        assertEquals(
            50,
            OnGoingChallenge(HumChallengeInternal.DiscoverGuidedBrushing, 50).progress
        )
    }

    @Test
    fun onGoingChallengeUseTitleDescriptionOnGoingAsTitleDescription() {
        val challenge = OnGoingChallenge(HumChallengeInternal.DiscoverGuidedBrushing, 10)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.titleDescriptionOnGoing
                    )
            ),
            challenge.titleDescription(context())
        )
    }

    @Test
    fun onGoingChallengeUseDescriptionOnGoingAsDescriptionWithHighlight() {
        val challenge = OnGoingChallenge(HumChallengeInternal.DiscoverGuidedBrushing, 10)

        assertEquals(
            highlightString(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionOnGoing
                    ),
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionHighlightOnGoing
                    )
            ).toString(),
            challenge.description(context()).toString()
        )
    }

    @Test
    fun onGoingChallengeUseEmptyStringAsActionText() {
        val challenge = OnGoingChallenge(HumChallengeInternal.BrushFor5Days, 10)

        val expectedEmptySpannable = SpannableStringBuilder("")
        assertEquals(expectedEmptySpannable, challenge.actionText(context()))
        assertFalse(challenge.hasActionButton())
    }

    @Test
    fun onGoingDiscoverGuidedBrushingHasActionButton() {
        val challenge = OnGoingChallenge(HumChallengeInternal.DiscoverGuidedBrushing, 0)

        val expectedString =
            context().getString(R.string.brushing_streak_discover_guided_brushing_accept_challenge)
        val expectedActionSpannable = SpannableStringBuilder(expectedString)
        assertEquals(expectedActionSpannable, challenge.actionText(context()))
        assertTrue(challenge.hasActionButton())
    }

    /*
    CompletedChallenge
    */

    @Test
    fun completeChallengeHave100Progress() {
        assertEquals(
            100,
            CompletedChallenge(HumChallengeInternal.DiscoverGuidedBrushing).progress
        )
    }

    @Test
    fun completeChallengeUseTitleDescriptionOnGoingAsTitleDescription() {
        val challenge = CompletedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.titleDescriptionCompleted
                    )
            ),
            challenge.titleDescription(context())
        )
    }

    @Test
    fun completeChallengeUseDescriptionOnGoingAsDescriptionWithHighlight() {
        val challenge = CompletedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            highlightString(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionCompleted
                    ),
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.descriptionHighlightCompleted
                    )
            ).toString(),
            challenge.description(context()).toString()
        )
    }

    @Test
    fun completeChallengeUseCompleteChallengeAsActionText() {
        val challenge = CompletedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

        assertEquals(
            SpannableStringBuilder(
                context()
                    .getString(
                        HumChallengeInternalResources.DiscoverGuidedBrushing.completeChallengeText
                    )
            ),
            challenge.actionText(context())
        )
    }
}
