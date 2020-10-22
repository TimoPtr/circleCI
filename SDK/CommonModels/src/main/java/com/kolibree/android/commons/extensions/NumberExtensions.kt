/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.commons.extensions

import androidx.annotation.Keep

@Keep
fun Double.zeroIfNan(): Double = if (isNaN()) 0.0 else this
