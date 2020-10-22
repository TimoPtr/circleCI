/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.feedback

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.coachplus.R
import junit.framework.TestCase.assertEquals
import org.junit.Test

class FeedBackMessageResourcesTest : BaseUnitTest() {

    @Test
    fun `each feedback message has corresponding resource object`() {
        assertEquals(
            FeedBackMessageResources.EmptyFeedback,
            FeedBackMessageResources.from(FeedBackMessage.EmptyFeedback)
        )
        assertEquals(
            FeedBackMessageResources.OutOfMouthFeedback,
            FeedBackMessageResources.from(FeedBackMessage.OutOfMouthFeedback)
        )
        assertEquals(
            FeedBackMessageResources.WrongZoneFeedback,
            FeedBackMessageResources.from(FeedBackMessage.WrongZoneFeedback)
        )
        assertEquals(
            FeedBackMessageResources.RinseBrushHeadFeedback,
            FeedBackMessageResources.from(FeedBackMessage.RinseBrushHeadFeedback)
        )
        assertEquals(
            FeedBackMessageResources.WrongHandleFeedback,
            FeedBackMessageResources.from(FeedBackMessage.WrongHandleFeedback)
        )
        assertEquals(
            FeedBackMessageResources.WrongIncisorsIntAngleFeedback,
            FeedBackMessageResources.from(FeedBackMessage.WrongIncisorsIntAngleFeedback)
        )
        assertEquals(
            FeedBackMessageResources.Wrong45AngleFeedback,
            FeedBackMessageResources.from(FeedBackMessage.Wrong45AngleFeedback)
        )
        assertEquals(
            FeedBackMessageResources.UnderSpeedFeedback,
            FeedBackMessageResources.from(FeedBackMessage.UnderSpeedFeedback)
        )
        assertEquals(
            FeedBackMessageResources.OverSpeedFeedback,
            FeedBackMessageResources.from(FeedBackMessage.OverSpeedFeedback)
        )
        assertEquals(
            FeedBackMessageResources.OverpressureFeedback,
            FeedBackMessageResources.from(FeedBackMessage.OverpressureFeedback)
        )
    }

    @Test
    fun `EmptyFeedbackMessage has correct resources`() {
        assertEquals(R.string.empty, FeedBackMessageResources.EmptyFeedback.message)
        assertEquals(R.raw.wrong_zone_detected, FeedBackMessageResources.EmptyFeedback.imageId)
    }

    @Test
    fun `OutOfMouthFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_out_of_mouth,
            FeedBackMessageResources.OutOfMouthFeedback.message
        )
        assertEquals(R.raw.animated_gif_out_of_mouth, FeedBackMessageResources.OutOfMouthFeedback.imageId)
    }

    @Test
    fun `WrongZoneFeedback has correct resources`() {
        assertEquals(R.string.mouth_zone_wrong, FeedBackMessageResources.WrongZoneFeedback.message)
        assertEquals(R.raw.wrong_zone_detected, FeedBackMessageResources.WrongZoneFeedback.imageId)
    }

    @Test
    fun `RinseBrushHeadFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_rinse_brush_head,
            FeedBackMessageResources.RinseBrushHeadFeedback.message
        )
        assertEquals(
            R.raw.animated_gif_rinse_brush_head,
            FeedBackMessageResources.RinseBrushHeadFeedback.imageId
        )
    }

    @Test
    fun `WrongHandleFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_wrong_handle,
            FeedBackMessageResources.WrongHandleFeedback.message
        )
        assertEquals(R.raw.animated_gif_plaqless, FeedBackMessageResources.WrongHandleFeedback.imageId)
    }

    @Test
    fun `WrongIncisorsIntAngleFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_wrong_angle_incisors_interior,
            FeedBackMessageResources.WrongIncisorsIntAngleFeedback.message
        )
        assertEquals(
            R.raw.animated_gif_incisors,
            FeedBackMessageResources.WrongIncisorsIntAngleFeedback.imageId
        )
    }

    @Test
    fun `Wrong45AngleFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_wrong_angle_other_zones,
            FeedBackMessageResources.Wrong45AngleFeedback.message
        )
        assertEquals(R.raw.animated_gif_molars, FeedBackMessageResources.Wrong45AngleFeedback.imageId)
    }

    @Test
    fun `UnderSpeedFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_speed_slow,
            FeedBackMessageResources.UnderSpeedFeedback.message
        )
        assertEquals(R.raw.animated_gif_speed_slow, FeedBackMessageResources.UnderSpeedFeedback.imageId)
    }

    @Test
    fun `OverSpeedFeedback has correct resources`() {
        assertEquals(
            R.string.coach_plus_feedback_message_speed_fast,
            FeedBackMessageResources.OverSpeedFeedback.message
        )
        assertEquals(R.raw.animated_gif_speed, FeedBackMessageResources.OverSpeedFeedback.imageId)
    }
}
