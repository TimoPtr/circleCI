/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.charts.inoff.domain.InOffBrushingsCountProvider
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
internal object EspresssoInOffBrushingsCountModule {

    val inOffBrushingsCountProvider: InOffBrushingsCountProvider = mock()

    @Provides
    @AppScope
    fun bindsInOffBrushingsCountProvider(): InOffBrushingsCountProvider = inOffBrushingsCountProvider
}
