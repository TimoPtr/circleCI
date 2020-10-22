/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.confirmation

import com.kolibree.android.angleandspeed.R
import com.kolibree.android.app.mvi.confirmation.GameConfirmationResourceProvider

internal object SpeedControlConfirmationResourceProvider : GameConfirmationResourceProvider {

    override fun summaryTextResId() = R.string.speed_control_confirmation_hint

    override fun summaryHighlightTextResId() = R.string.speed_control_confirmation_hint_highlight

    override fun drawableResId() = R.drawable.illustr_analyzingdata
}
