/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.widget.progressbar

import androidx.annotation.Keep

/**
 * Defines the state of [ProgressAnimator].
 * @see [ProgressAnimator]
 */
@Keep
enum class ProgressState {
    START,
    PAUSE,
    RESET,
    DESTROY
}
