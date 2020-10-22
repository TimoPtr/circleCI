/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.controller

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.coachplus.feedback.FeedBackMessage
import com.kolibree.kml.MouthZone16

/**
 * Object that represents the state of the CoachPlusController
 */
@VisibleForApp
data class CoachPlusControllerResult(
    val zoneToBrush: MouthZone16,
    val completionPercent: Int,
    val brushingGoodZone: Boolean,
    val sequenceFinished: Boolean,
    val feedBackMessage: FeedBackMessage
)
