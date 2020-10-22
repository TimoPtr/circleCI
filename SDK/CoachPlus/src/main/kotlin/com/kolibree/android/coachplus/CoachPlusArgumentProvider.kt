/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus

import androidx.annotation.Keep
import com.kolibree.android.coachplus.ui.CoachPlusColorSet
import com.kolibree.android.commons.ToothbrushModel

@Keep
interface CoachPlusArgumentProvider {

    fun provideManualMode(): Boolean

    fun provideToothbrushMac(): String?

    fun provideToothbrushModel(): ToothbrushModel?

    fun provideColorSet(): CoachPlusColorSet
}
