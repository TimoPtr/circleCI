/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
interface GameSplashResourcesProvider {
    @DrawableRes
    fun background(game: MiddlewareUnityGame): Int

    @StringRes
    fun loadingText(): Int
}
