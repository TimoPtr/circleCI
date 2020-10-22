/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personallchallenge.presention

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallengeInternalResources
import com.kolibree.android.rewards.personalchallenge.presentation.HumChallengeRecommendationAction
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class HumChallengeInternalResourcesTest : BaseUnitTest() {

    @Test
    fun `DiscoverGuidedBrushing have the right resources`() {
        with(HumChallengeInternalResources.DiscoverGuidedBrushing) {
            assertEquals(R.string.brushing_streak_title_for_1_day_challenge, title)
            assertEquals(R.string.brushing_streak_discover_guided_brushing_subtitle, subTitle)
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_guided_brushing_accept_challenge,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.COACH_PLUS, action)
        }
    }

    @Test
    fun `DiscoverOfflineBrushing have the right resources`() {
        with(HumChallengeInternalResources.DiscoverOfflineBrushing) {
            assertEquals(R.string.brushing_streak_title_for_1_day_challenge, title)
            assertEquals(R.string.brushing_streak_discover_offline_brushing_subtitle, subTitle)
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_title_description_not_accepted,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_offline_brushing_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.empty,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.NOTHING, action)
        }
    }

    @Test
    fun `BrushFor5Days have the right resources`() {
        with(HumChallengeInternalResources.BrushFor5Days) {
            assertEquals(R.string.brushing_streak_title_for_5_days_challenge, title)
            assertEquals(R.string.brushing_streak_five_consecutive_days_subtitle, subTitle)
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_title_description_ongoing,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.empty,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.NOTHING, action)
        }
    }

    @Test
    fun `BrushFor5DaysAtLeast80Coverage have the right resources`() {
        with(HumChallengeInternalResources.BrushFor5DaysAtLeast80Coverage) {
            assertEquals(
                R.string.brushing_streak_title_for_5_days_challenge,
                title
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_subtitle,
                subTitle
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_title_description_ongoing,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.empty,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.NOTHING, action)
        }
    }

    @Test
    fun `BrushTwiceADayFor5Days have the right resources`() {
        with(HumChallengeInternalResources.BrushTwiceADayFor5Days) {
            assertEquals(R.string.brushing_streak_title_for_5_days_challenge, title)
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_subtitle,
                subTitle
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_title_description_ongoing,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_brush_twice_a_day_for_five_days_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.empty,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.NOTHING, action)
        }
    }

    @Test
    fun `BrushTwiceADayFor5DaysAtLeast80Coverage have the right resources`() {
        with(HumChallengeInternalResources.BrushTwiceADayFor5DaysAtLeast80Coverage) {
            assertEquals(
                R.string.brushing_streak_title_for_5_days_challenge,
                title
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_subtitle,
                subTitle
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_subtitle_highlight,
                subTitleHighlight
            )
            assertEquals(
                R.string.brushing_streak_title_description_not_accepted,
                titleDescriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_title_description_ongoing,
                titleDescriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_title_description_completed,
                titleDescriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing,
                descriptionNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing_highlight,
                descriptionHighlightNotAccepted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing,
                descriptionOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing_highlight,
                descriptionHighlightOnGoing
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_complete,
                descriptionCompleted
            )
            assertEquals(
                R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_complete_highlight,
                descriptionHighlightCompleted
            )
            assertEquals(
                R.string.brushing_streak_discover_accept_challenge,
                acceptChallengeText
            )
            assertEquals(
                R.string.empty,
                actionChallengeText
            )
            assertEquals(
                R.string.brushing_streak_discover_complete_challenge,
                completeChallengeText
            )
            assertEquals(HumChallengeRecommendationAction.NOTHING, action)
        }
    }
}
