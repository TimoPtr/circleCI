/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.unity.UnityGame

@VisibleForApp
enum class MiddlewareUnityGame {
    ARCHAELOGY,
    PIRATE,
    RABBIDS
}

internal fun MiddlewareUnityGame.toUnityGame(): UnityGame =
    when (this) {
        MiddlewareUnityGame.ARCHAELOGY -> UnityGame.Archaelogy
        MiddlewareUnityGame.PIRATE -> UnityGame.Pirate
        MiddlewareUnityGame.RABBIDS -> UnityGame.Rabbids
    }
