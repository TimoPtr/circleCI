/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.unity

import com.kolibree.R
import com.kolibree.android.unity.GameSplashResourcesProvider
import com.kolibree.android.unity.MiddlewareUnityGame
import com.kolibree.android.unity.MiddlewareUnityGame.ARCHAELOGY
import com.kolibree.android.unity.MiddlewareUnityGame.PIRATE
import com.kolibree.android.unity.MiddlewareUnityGame.RABBIDS
import javax.inject.Inject

internal class GameSplashResourcesProviderImpl @Inject constructor() : GameSplashResourcesProvider {
    override fun background(game: MiddlewareUnityGame): Int =
        when (game) {
            ARCHAELOGY -> R.drawable.pirate_splash_screen
            PIRATE -> R.drawable.pirate_splash_screen
            RABBIDS -> R.drawable.pirate_splash_screen
        }

    override fun loadingText(): Int = R.string.please_wait
}
