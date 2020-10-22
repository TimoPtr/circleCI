/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.presentation

import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.rewards.R
import com.kolibree.android.rewards.personalchallenge.model.HumChallengeInternal

@VisibleForApp
enum class HumChallengeRecommendationAction {
    NOTHING,
    COACH_PLUS;
}

@Suppress("MaxLineLength")
internal enum class HumChallengeInternalResources(
    @StringRes val title: Int, // brushing streak Or discover activity

    @StringRes val subTitle: Int, // in blue section
    @StringRes val subTitleHighlight: Int,

    @StringRes val titleDescriptionNotAccepted: Int,

    @StringRes val titleDescriptionOnGoing: Int,

    @StringRes val titleDescriptionCompleted: Int,

    @StringRes val descriptionNotAccepted: Int,
    @StringRes val descriptionHighlightNotAccepted: Int,

    @StringRes val descriptionOnGoing: Int,
    @StringRes val descriptionHighlightOnGoing: Int,

    @StringRes val descriptionCompleted: Int,
    @StringRes val descriptionHighlightCompleted: Int,

    // Buttons
    @StringRes val acceptChallengeText: Int, // text on the button might be go to somewhere - accept challenge
    @StringRes val actionChallengeText: Int, // text on the button might be go to somewhere - accept challenge
    @StringRes val completeChallengeText: Int, // text on the button might be go to somewhere - accept challenge,

    val action: HumChallengeRecommendationAction
) {
    DiscoverGuidedBrushing(
        title = R.string.brushing_streak_title_for_1_day_challenge,
        subTitle = R.string.brushing_streak_discover_guided_brushing_subtitle,
        subTitleHighlight = R.string.brushing_streak_discover_guided_brushing_subtitle_highlight,
        titleDescriptionNotAccepted = R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_discover_guided_brushing_title_description_not_accepted,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_discover_guided_brushing_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_discover_guided_brushing_description_ongoing_highlight,

        descriptionOnGoing = R.string.brushing_streak_discover_guided_brushing_description_ongoing,
        descriptionHighlightOnGoing = R.string.brushing_streak_discover_guided_brushing_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_discover_guided_brushing_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_discover_guided_brushing_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_accept_challenge,
        actionChallengeText = R.string.brushing_streak_discover_guided_brushing_accept_challenge,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.COACH_PLUS
    ),
    DiscoverOfflineBrushing(
        title = R.string.brushing_streak_title_for_1_day_challenge,
        subTitle = R.string.brushing_streak_discover_offline_brushing_subtitle,
        subTitleHighlight = R.string.brushing_streak_discover_offline_brushing_subtitle_highlight,
        titleDescriptionNotAccepted = R.string.brushing_streak_discover_offline_brushing_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_discover_offline_brushing_title_description_not_accepted,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_discover_offline_brushing_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_discover_offline_brushing_description_ongoing_highlight,

        descriptionOnGoing = R.string.brushing_streak_discover_offline_brushing_description_ongoing,
        descriptionHighlightOnGoing = R.string.brushing_streak_discover_offline_brushing_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_discover_offline_brushing_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_discover_offline_brushing_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_discover_accept_challenge,
        actionChallengeText = R.string.empty,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.NOTHING
    ),
    BrushFor5Days(
        title = R.string.brushing_streak_title_for_5_days_challenge,
        subTitle = R.string.brushing_streak_five_consecutive_days_subtitle,
        subTitleHighlight = R.string.brushing_streak_five_consecutive_days_subtitle_highlight,

        titleDescriptionNotAccepted = R.string.brushing_streak_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_title_description_ongoing,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_five_consecutive_days_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_five_consecutive_days_description_ongoing_highlight,

        descriptionOnGoing = R.string.brushing_streak_five_consecutive_days_description_ongoing,
        descriptionHighlightOnGoing = R.string.brushing_streak_five_consecutive_days_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_five_consecutive_days_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_five_consecutive_days_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_discover_accept_challenge,
        actionChallengeText = R.string.empty,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.NOTHING
    ),
    BrushFor5DaysAtLeast80Coverage(
        title = R.string.brushing_streak_title_for_5_days_challenge,
        subTitle = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_subtitle,
        subTitleHighlight = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_subtitle_highlight,

        titleDescriptionNotAccepted = R.string.brushing_streak_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_title_description_ongoing,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing_highlight,

        descriptionOnGoing =
        R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing,
        descriptionHighlightOnGoing =
        R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_five_consecutive_days_once_a_day_eighty_percent_coverage_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_discover_accept_challenge,
        actionChallengeText = R.string.empty,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.NOTHING
    ),
    BrushTwiceADayFor5Days(
        title = R.string.brushing_streak_title_for_5_days_challenge,
        subTitle = R.string.brushing_streak_brush_twice_a_day_for_five_days_subtitle,
        subTitleHighlight = R.string.brushing_streak_brush_twice_a_day_for_five_days_subtitle_highlight,

        titleDescriptionNotAccepted = R.string.brushing_streak_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_title_description_ongoing,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing_highlight,

        descriptionOnGoing = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing,
        descriptionHighlightOnGoing = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_brush_twice_a_day_for_five_days_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_discover_accept_challenge,
        actionChallengeText = R.string.empty,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.NOTHING
    ),
    BrushTwiceADayFor5DaysAtLeast80Coverage(
        title = R.string.brushing_streak_title_for_5_days_challenge,
        subTitle = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_subtitle,
        subTitleHighlight = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_subtitle_highlight,

        titleDescriptionNotAccepted = R.string.brushing_streak_title_description_not_accepted,
        titleDescriptionOnGoing = R.string.brushing_streak_title_description_ongoing,
        titleDescriptionCompleted = R.string.brushing_streak_title_description_completed,

        descriptionNotAccepted = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing,
        descriptionHighlightNotAccepted = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing_highlight,

        descriptionOnGoing = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing,
        descriptionHighlightOnGoing = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_ongoing_highlight,

        descriptionCompleted = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_complete,
        descriptionHighlightCompleted = R.string.brushing_streak_five_consecutive_days_twice_a_day_eighty_percent_coverage_description_complete_highlight,

        acceptChallengeText = R.string.brushing_streak_discover_accept_challenge,
        actionChallengeText = R.string.empty,
        completeChallengeText = R.string.brushing_streak_discover_complete_challenge,

        action = HumChallengeRecommendationAction.NOTHING
    );

    internal companion object {
        fun from(challenge: HumChallengeInternal): HumChallengeInternalResources =
            when (challenge) {
                HumChallengeInternal.DiscoverGuidedBrushing -> DiscoverGuidedBrushing
                HumChallengeInternal.DiscoverOfflineBrushing -> DiscoverOfflineBrushing
                HumChallengeInternal.BrushFor5Days -> BrushFor5Days
                HumChallengeInternal.BrushFor5DaysAtLeast80Coverage -> BrushFor5DaysAtLeast80Coverage
                HumChallengeInternal.BrushTwiceADayFor5Days -> BrushTwiceADayFor5Days
                HumChallengeInternal.BrushTwiceADayFor5DaysAtLeast80Coverage -> BrushTwiceADayFor5DaysAtLeast80Coverage
            }
    }
}
