/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package cn.colgate.colgateconnect.orphanbrushings

import com.kolibree.android.utils.KolibreeAppVersions
import dagger.Module
import dagger.Provides

@Module
internal object OrphanBrushingsActivityModule {

    @JvmStatic
    @Provides
    internal fun providesAppVersions(activity: OrphanBrushingsActivity): KolibreeAppVersions =
        KolibreeAppVersions(activity)
}
