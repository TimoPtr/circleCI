/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.presentation

import android.content.Context
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.ui.text.highlightString
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingEvent
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.personalchallenge.model.HumChallengeInternal
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

/**
 * This class represent a HumChallenge, it expose all the resources needed and the state of
 * the challenge
 */
@VisibleForApp
sealed class HumChallenge(
    internal open val challenge: HumChallengeInternal,
    open val progress: Int
) : Parcelable {
    internal val resources: HumChallengeInternalResources by lazy {
        HumChallengeInternalResources.from(challenge)
    }

    fun title(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.title))

    fun subTitle(context: Context): Spannable =
        highlightString(
            context.getString(resources.subTitle),
            context.getString(resources.subTitleHighlight)
        )

    abstract fun titleDescription(context: Context): Spannable

    abstract fun description(context: Context): Spannable

    abstract fun actionText(context: Context): Spannable

    abstract fun hasActionButton(): Boolean

    val action: HumChallengeRecommendationAction by lazy { resources.action }

    val smiles: Int by lazy { challenge.smiles }

    fun isMoreThanOneDay(): Boolean = challenge.period != PersonalChallengePeriod.ONE_DAY

    fun durationInDays() = challenge.period.duration.toDays().toInt()

    internal companion object {
        fun fromV1PersonalChallenge(challenge: V1PersonalChallenge): HumChallenge? {
            val challengeInternal = HumChallengeInternal.fromV1PersonalChallenge(challenge)
            return when {
                challengeInternal == null -> {
                    Timber.w("There is a V1Challenge that doesn't match any HumChallenge $challenge")
                    null
                }
                challenge.completed -> {
                    CompletedChallenge(challengeInternal)
                }
                else -> {
                    OnGoingChallenge(challengeInternal, challenge.progress)
                }
            }
        }
    }
}

@VisibleForApp
@Parcelize
class NotAcceptedChallenge internal constructor(override val challenge: HumChallengeInternal) :
    HumChallenge(challenge, 0) {

    override fun titleDescription(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.titleDescriptionNotAccepted))

    override fun description(context: Context): Spannable =
        highlightString(
            context.getString(resources.descriptionNotAccepted),
            context.getString(resources.descriptionHighlightNotAccepted)
        )

    override fun actionText(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.acceptChallengeText))

    override fun hasActionButton() = true

    internal companion object {
        fun fromBrushingEvent(events: List<BrushingEvent>): NotAcceptedChallenge? =
            HumChallengeInternal.fromEvents(events)
                .minBy { it.priority }?.let {
                    NotAcceptedChallenge(it)
                }
    }
}

@VisibleForApp
@Parcelize
class OnGoingChallenge internal constructor(
    override val challenge: HumChallengeInternal,
    override val progress: Int
) :
    HumChallenge(challenge, progress) {

    override fun titleDescription(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.titleDescriptionOnGoing))

    override fun description(context: Context): Spannable =
        highlightString(
            context.getString(resources.descriptionOnGoing),
            context.getString(resources.descriptionHighlightOnGoing)
        )

    override fun actionText(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.actionChallengeText))

    override fun hasActionButton() = resources.action != HumChallengeRecommendationAction.NOTHING
}

@VisibleForApp
@Parcelize
class CompletedChallenge internal constructor(override val challenge: HumChallengeInternal) :
    HumChallenge(challenge, 100) {

    override fun titleDescription(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.titleDescriptionCompleted))

    override fun description(context: Context): Spannable =
        highlightString(
            context.getString(resources.descriptionCompleted),
            context.getString(resources.descriptionHighlightCompleted)
        )

    override fun actionText(context: Context): Spannable =
        SpannableStringBuilder(context.getString(resources.completeChallengeText))

    override fun hasActionButton() = true
}

@VisibleForTesting
@Keep
fun notAcceptedDiscoverGuidedBrushingChallenge() =
    NotAcceptedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

@VisibleForTesting
@Keep
fun onGoingDiscoverGuidedBrushingChallenge() =
    OnGoingChallenge(HumChallengeInternal.DiscoverGuidedBrushing, 0)

@VisibleForTesting
@Keep
fun completedDiscoverGuidedBrushingChallenge() =
    CompletedChallenge(HumChallengeInternal.DiscoverGuidedBrushing)

@VisibleForTesting
@Keep
fun onGoingBrushFor5Days(progress: Int) =
    OnGoingChallenge(HumChallengeInternal.BrushFor5Days, progress)
