/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.sounds

import com.kolibree.android.coachplus.utils.MusicHintProvider
import com.kolibree.android.sdk.e1.ToothbrushShutdownValveModule
import dagger.Module
import dagger.Provides

@Module(includes = [ToothbrushShutdownValveModule::class])
internal object CoachPlusSoundsModule {
    @Provides
    fun providesToothbrushSessionMac(activity: CoachSoundsSettingsActivity): String? {
        return activity.readMacFromIntent()
    }

    @Provides
    fun provideMusicHint(): MusicHintProvider = V1MusicHintProvider
}
