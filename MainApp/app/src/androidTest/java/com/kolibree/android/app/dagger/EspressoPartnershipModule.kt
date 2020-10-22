/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.content.Context
import androidx.room.Room
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.data.api.PartnershipApiFake
import com.kolibree.android.partnerships.data.di.PartnershipImplementationsModule
import com.kolibree.android.partnerships.data.di.PartnershipLogicModule
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspaceDatabase
import dagger.Module
import dagger.Provides

@Module(includes = [PartnershipLogicModule::class, PartnershipImplementationsModule::class])
internal object EspressoPartnershipModule {

    @Provides
    fun provideApi(): PartnershipApi = PartnershipApiFake

    @Provides
    fun providePartnershipApiFake(): PartnershipApiFake = PartnershipApiFake

    @Provides
    @AppScope
    fun providesPartnershipsDatabase(context: Context): HeadspaceDatabase {
        return Room.inMemoryDatabaseBuilder(context, HeadspaceDatabase::class.java)
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }
}
