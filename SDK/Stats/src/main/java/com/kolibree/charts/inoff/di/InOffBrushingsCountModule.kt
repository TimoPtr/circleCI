/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.inoff.di

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.charts.inoff.data.api.InOffBrushingsCountApi
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.domain.InOffBrushingsCountProvider
import com.kolibree.charts.inoff.domain.InOffBrushingsCountProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module
internal abstract class InOffBrushingsCountModule {

    @Binds
    abstract fun bindsInOffBrushingsCountProvider(impl: InOffBrushingsCountProviderImpl): InOffBrushingsCountProvider

    @Binds
    @IntoSet
    abstract fun bindsTruncable(dao: InOffBrushingsCountDao): Truncable

    internal companion object {

        @Provides
        fun providesInOffBrushingsCountApi(retrofit: Retrofit): InOffBrushingsCountApi =
            retrofit.create(InOffBrushingsCountApi::class.java)
    }
}
