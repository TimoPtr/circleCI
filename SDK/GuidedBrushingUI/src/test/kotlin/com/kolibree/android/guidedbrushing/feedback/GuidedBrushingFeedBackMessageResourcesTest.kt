/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.guidedbrushing.feedback

import com.kolibree.android.app.feedback.FeedbackMessageResource
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.guidedbrushing.R
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GuidedBrushingFeedBackMessageResourcesTest : BaseUnitTest() {

    @Test
    fun `supported feedback messages have corresponding resource object`() {
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.EmptyFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.message,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.EmptyFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.WrongZoneFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.WrongZoneFeedback.message,
                GuidedBrushingFeedBackMessageResources.WrongZoneFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.WrongZoneFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.WrongIncisorsIntAngleFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.WrongIncisorsIntAngleFeedback.message,
                GuidedBrushingFeedBackMessageResources.WrongIncisorsIntAngleFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.WrongIncisorsIntAngleFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.Wrong45AngleFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.Wrong45AngleFeedback.message,
                GuidedBrushingFeedBackMessageResources.Wrong45AngleFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.Wrong45AngleFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.UnderSpeedFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.UnderSpeedFeedback.message,
                GuidedBrushingFeedBackMessageResources.UnderSpeedFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.UnderSpeedFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.OverSpeedFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.OverSpeedFeedback.message,
                GuidedBrushingFeedBackMessageResources.OverSpeedFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.OverSpeedFeedback)
        )
    }

    @Test
    fun `unsupported feedback messages return empty feedback resources`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.EmptyFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.message,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.OutOfMouthFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.EmptyFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.message,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.RinseBrushHeadFeedback)
        )
        assertEquals(
            FeedbackMessageResource(
                FeedBackMessage.EmptyFeedback.shouldShow,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.message,
                GuidedBrushingFeedBackMessageResources.EmptyFeedback.imageId
            ),
            GuidedBrushingFeedBackMessageResources.from(FeedBackMessage.WrongHandleFeedback)
        )
    }

    @Test
    fun `EmptyFeedbackMessage has correct resources`() {
        assertEquals(R.string.guided_brushing_no_feedback, GuidedBrushingFeedBackMessageResources.EmptyFeedback.message)
        assertEquals(R.drawable.ic_feedback_all_good, GuidedBrushingFeedBackMessageResources.EmptyFeedback.imageId)
    }

    @Test
    fun `WrongZoneFeedback has correct resources`() {
        assertEquals(
            R.string.guided_brushing_wrong_zone_feedback,
            GuidedBrushingFeedBackMessageResources.WrongZoneFeedback.message
        )
        assertEquals(
            R.drawable.ic_feedback_wrong_zone,
            GuidedBrushingFeedBackMessageResources.WrongZoneFeedback.imageId
        )
    }

    @Test
    fun `WrongIncisorsIntAngleFeedback has correct resources`() {
        assertEquals(
            R.string.guided_brushing_wrong_angle_feedback,
            GuidedBrushingFeedBackMessageResources.WrongIncisorsIntAngleFeedback.message
        )
        assertEquals(
            R.drawable.ic_feedback_wrong_angle,
            GuidedBrushingFeedBackMessageResources.WrongIncisorsIntAngleFeedback.imageId
        )
    }

    @Test
    fun `Wrong45AngleFeedback has correct resources`() {
        assertEquals(
            R.string.guided_brushing_wrong_angle_feedback,
            GuidedBrushingFeedBackMessageResources.Wrong45AngleFeedback.message
        )
        assertEquals(
            R.drawable.ic_feedback_wrong_angle,
            GuidedBrushingFeedBackMessageResources.Wrong45AngleFeedback.imageId
        )
    }

    @Test
    fun `UnderSpeedFeedback has correct resources`() {
        assertEquals(
            R.string.guided_brushing_too_slow_feedback,
            GuidedBrushingFeedBackMessageResources.UnderSpeedFeedback.message
        )
        assertEquals(
            R.drawable.ic_feedback_too_slow,
            GuidedBrushingFeedBackMessageResources.UnderSpeedFeedback.imageId
        )
    }

    @Test
    fun `OverSpeedFeedback has correct resources`() {
        assertEquals(
            R.string.guided_brushing_too_fast_feedback,
            GuidedBrushingFeedBackMessageResources.OverSpeedFeedback.message
        )
        assertEquals(
            R.drawable.ic_feedback_too_fast,
            GuidedBrushingFeedBackMessageResources.OverSpeedFeedback.imageId
        )
    }
}
