/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.testangles.mvi.confirmation

import com.kolibree.android.angleandspeed.R
import com.kolibree.android.app.mvi.confirmation.GameConfirmationResourceProvider

internal object TestAnglesConfirmationResourceProvider : GameConfirmationResourceProvider {

    override fun summaryTextResId() = R.string.test_angles_confirmation_hint

    override fun summaryHighlightTextResId() = R.string.test_angles_confirmation_hint_highlight

    override fun drawableResId() = R.drawable.illustr_analyzingdata
}
