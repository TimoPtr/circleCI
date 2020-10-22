/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.test

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import com.kolibree.android.test.utils.ReflectionUtils

@Keep
fun ViewModel.invokeOnCleared() {
    ReflectionUtils.invokeProtectedVoidMethod(this, "onCleared")
}
