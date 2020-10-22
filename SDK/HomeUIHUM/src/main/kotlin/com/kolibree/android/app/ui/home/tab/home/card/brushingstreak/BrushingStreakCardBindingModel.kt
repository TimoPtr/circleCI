/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.card.brushingstreak

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.rewards.personalchallenge.presentation.CompletedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.NotAcceptedChallenge
import com.kolibree.android.rewards.personalchallenge.presentation.OnGoingChallenge
import kotlinx.android.parcel.Parcelize

@Suppress("TooManyFunctions")
@Parcelize
internal data class BrushingStreakCardBindingModel(
    val viewState: BrushingStreakCardViewState,
    override val layoutId: Int = R.layout.home_brushing_streak
) : DynamicCardBindingModel(viewState) {

    fun getPoints(context: Context): String {
        val points = viewState.challenge?.smiles ?: 0
        return context.getString(R.string.activities_task_points, points.toString())
    }

    fun isExpanded() = viewState.isExpanded

    fun isProposal() = viewState.challenge is NotAcceptedChallenge

    fun title(context: Context): Spannable {
        return viewState.challenge?.title(context).orEmpty()
    }

    fun collapsedDescription(context: Context): Spannable {
        return viewState.challenge?.subTitle(context).orEmpty()
    }

    fun expandedTitle(context: Context): Spannable {
        return viewState.challenge?.titleDescription(context).orEmpty()
    }

    fun expandedDescription(context: Context): Spannable {
        return viewState.challenge?.description(context).orEmpty()
    }

    fun actionButtonText(context: Context): Spannable {
        return viewState.challenge?.actionText(context).orEmpty()
    }

    fun challengeProgression(): BrushingStreakProgression {
        val challengeProgress = viewState.challenge?.progress ?: 0
        val durationInDays = viewState.challenge?.durationInDays() ?: 0
        val progress = challengeProgress / MAX_PROGRESS
        val progressInDays = progress * durationInDays
        return BrushingStreakProgression(progressInDays.toInt())
    }

    fun isMultiDaysChallenge(): Boolean {
        return viewState.challenge?.isMoreThanOneDay() ?: false
    }

    fun hasActionButton(): Boolean {
        return viewState.challenge?.hasActionButton() ?: false
    }

    fun onActionItemClick(interaction: ChallengeInteraction) {
        when (val challenge = viewState.challenge) {
            is NotAcceptedChallenge -> interaction.onAcceptChallengeClick(challenge)
            is OnGoingChallenge -> interaction.onActionClick(challenge)
            is CompletedChallenge -> interaction.onCompleteChallengeClick(challenge)
        }
    }
}

private fun Spannable?.orEmpty(): Spannable {
    return this ?: SpannableStringBuilder("")
}

private const val MAX_PROGRESS = 100f
