/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test

import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.offlinebrushings.persistence.EspressoOfflineBrushingsRepositoriesModule
import com.kolibree.android.test.dagger.EspressoDaggerInitializer

internal class KolibreeEspressoTestApplication : BaseKolibreeApplication() {

    override fun initDagger() {
        appComponent = EspressoDaggerInitializer.initialize(
            InstrumentationRegistry.getInstrumentation().targetContext,
            EspressoOfflineBrushingsRepositoriesModule(true)
        )
    }
}
