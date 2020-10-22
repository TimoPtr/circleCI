/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import androidx.annotation.VisibleForTesting
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.SupervisedResult16
import org.threeten.bp.Duration

/**
 * This class deal with all the logic of Feedback describe in the specification
 * (https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2735730/Coach+evolution#Coach+evolution-Businessrules)
 * It should be use in a CoachPlusKMLController and be call each time a new SupervisedResult is return by
 * KML
 *
 * WARNING : This class is not thread safe
 *
 * @param tickPeriod the period between two call, it will be use to increment currentTime each time the controller
 * calls the Mapper
 */
internal class CoachPlusFeedbackMapper(private val tickPeriod: Duration) {
    @VisibleForTesting
    internal var currentTime: Duration = Duration.ZERO
    @VisibleForTesting
    internal var cachedFeedbackMessage: FeedBackMessage = FeedBackMessage.EmptyFeedback
    @VisibleForTesting
    internal var receivedAt: Duration? = null
    @VisibleForTesting
    internal var startDisplayAt: Duration? = null

    /**
     * This method should be called when the user brush the right zone, it will return the feedback to display
     *
     * @param supervisedResult16 the result from KML that contains the KPI needed to create the feedback's
     * @param currentZone the current zone where the use is
     * @param plaqlessError the current plaqlessError state
     * @return the feedback to use
     */
    fun onGoodZoneBrushing(
        supervisedResult16: SupervisedResult16?,
        currentZone: MouthZone16,
        plaqlessError: PlaqlessError
    ): FeedBackMessage {
        onTick()
        return if (plaqlessError == PlaqlessError.OUT_OF_MOUTH) {
            FeedBackMessage.OutOfMouthFeedback
        } else {
            getFeedbackToDisplay(getFeedbackMessages(supervisedResult16, currentZone, plaqlessError))
        }
    }

    /**
     * This method should be called when the user brush the wrong zone
     *
     * @param supervisedResult16 the result from KML that contains the KPI needed to create the feedback's
     * @param currentZone the current zone where the use is
     * @param plaqlessError the current plaqlessError state
     * @return the feedback to use
     */
    fun onWrongZoneBrushing(
        supervisedResult16: SupervisedResult16?,
        currentZone: MouthZone16,
        plaqlessError: PlaqlessError
    ): FeedBackMessage {
        onTick()

        if (plaqlessError == PlaqlessError.OUT_OF_MOUTH) {
            return FeedBackMessage.OutOfMouthFeedback
        }

        val feedbacks = arrayListOf<FeedBackMessage>()
        feedbacks.addAll(getFeedbackMessages(supervisedResult16, currentZone, plaqlessError))
        feedbacks.add(FeedBackMessage.WrongZoneFeedback)
        return getFeedbackToDisplay(feedbacks)
    }

    fun reset() {
        currentTime = Duration.ZERO
        cachedFeedbackMessage = FeedBackMessage.EmptyFeedback
        receivedAt = null
        startDisplayAt = null
    }

    /**
     * Increase the currentTime with the tickPeriod given in params
     */
    @VisibleForTesting
    fun onTick() {
        currentTime = currentTime.plus(tickPeriod)
    }

    @VisibleForTesting
    fun getFeedbackMessages(
        supervisedResult16: SupervisedResult16?,
        currentZone: MouthZone16,
        plaqlessError: PlaqlessError
    ): List<FeedBackMessage> {
        val feedbacks = arrayListOf<FeedBackMessage>()

        if (supervisedResult16?.optionalKpi?.first == true) {
            val kpis = supervisedResult16.optionalKpi.second
            getAngleFeedback(kpis.isOrientationCorrect, currentZone)?.let { feedbacks.add(it) }
            getSpeedFeedback(kpis.speedCorrectness)?.let { feedbacks.add(it) }
            getGlintFeedback(kpis.isPressureCorrect)?.let { feedbacks.add(it) }
        }
        getPlaqlessFeedback(plaqlessError)?.let { feedbacks.add(it) }

        return feedbacks
    }

    private fun getGlintFeedback(pressureCorrect: Boolean): FeedBackMessage? =
        FeedBackMessage.OverpressureFeedback.takeIf { !pressureCorrect }

    /**
     * This function follow apply the priority rules and times rules from
     * https://kolibree.atlassian.net/wiki/spaces/PROD/pages/2735730/Coach+evolution#Coach+evolution-Businessrules
     *
     * @param feedbacks all the feedbacks that are available for displaying
     * @return the feedback to display
     */
    @VisibleForTesting
    fun getFeedbackToDisplay(feedbacks: List<FeedBackMessage>): FeedBackMessage {
        val durationSinceReceived = currentTime.minus(receivedAt ?: currentTime)
        val durationSinceDisplay = currentTime.minus(startDisplayAt ?: currentTime)

        val isSameAsPrevious = feedbacks.contains(cachedFeedbackMessage)
        val mostImportantFeedback = feedbacks.minBy { it.priorityLevel }

        return when {
            shouldContinueDisplayFeedback(durationSinceDisplay) -> {
                // A message is already display we should continue display it
                if (isSameAsPrevious) {
                    maybeUpdateStartDisplayAt()
                }
                cachedFeedbackMessage
            }
            shouldCacheFeedback(isSameAsPrevious, mostImportantFeedback) -> {
                // there was no message that should be displayed take the feedback
                // with the most important priority
                receivedAt = currentTime
                startDisplayAt = null
                cachedFeedbackMessage = mostImportantFeedback!! // nullability check in shouldCacheFeedback
                FeedBackMessage.EmptyFeedback
            }
            shouldStartDisplay(isSameAsPrevious, durationSinceReceived) -> {
                // we received the same message as the one cached and
                // it has been more than minDurationBeforeDisplay since the reception so we display it

                startDisplayAt = currentTime
                cachedFeedbackMessage
            }
            else -> // it's the same message as before but less than minDurationBeforeDisplay
                // so we just don't display it and wait until we received this same feedback (or not)
                FeedBackMessage.EmptyFeedback
        }
    }

    @VisibleForTesting
    fun shouldContinueDisplayFeedback(durationSinceDisplay: Duration): Boolean =
        durationSinceDisplay > Duration.ZERO && durationSinceDisplay <= maxDurationDisplayed

    @VisibleForTesting
    fun shouldCacheFeedback(isSameFeedbackAsPrevious: Boolean, mostImportantFeedback: FeedBackMessage?): Boolean =
        !isSameFeedbackAsPrevious &&
            mostImportantFeedback != null &&
            mostImportantFeedback != FeedBackMessage.EmptyFeedback

    @VisibleForTesting
    fun shouldStartDisplay(isSameFeedbackAsPrevious: Boolean, durationSinceReceived: Duration): Boolean =
        isSameFeedbackAsPrevious && durationSinceReceived > minDurationBeforeDisplay

    /**
     * This method check if we should update the startDisplayAt field so the feedback stay longer (+extendTime) display
     */
    @VisibleForTesting
    fun maybeUpdateStartDisplayAt() {
        // We continue to display this message for extendDurationDisplay more since it's the same than the one cached
        val normalEnd = startDisplayAt?.plus(maxDurationDisplayed)
        val extendEnd = currentTime.plus(extendDurationDisplay)

        if (normalEnd != null && normalEnd < extendEnd) {
            startDisplayAt = currentTime.minus(extendDurationDisplay)
        }
    }

    companion object {
        @VisibleForTesting
        internal val minDurationBeforeDisplay: Duration = Duration.ofSeconds(1, 500000000)
        @VisibleForTesting
        internal val maxDurationDisplayed: Duration = Duration.ofSeconds(2)
        @VisibleForTesting
        internal val extendDurationDisplay: Duration = Duration.ofSeconds(1)
    }
}
