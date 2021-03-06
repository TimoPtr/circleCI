/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.feedback

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes

@Keep
data class FeedbackMessageResource(
    val shouldShow: Boolean,
    @StringRes val message: Int,
    @DrawableRes val imageId: Int
)
