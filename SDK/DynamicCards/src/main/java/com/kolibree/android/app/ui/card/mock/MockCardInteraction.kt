/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.card.mock

import android.annotation.SuppressLint
import com.kolibree.android.app.ui.card.DynamicCardInteraction

@SuppressLint("DeobfuscatedPublicSdkClass")
interface MockCardInteraction :
    DynamicCardInteraction {

    fun onCardClick()
}
