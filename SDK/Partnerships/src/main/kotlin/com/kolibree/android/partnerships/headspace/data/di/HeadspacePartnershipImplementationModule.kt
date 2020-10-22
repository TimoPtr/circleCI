/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.headspace.data.di

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.partnerships.data.api.PartnershipApiMapper
import com.kolibree.android.partnerships.data.di.PartnerKey
import com.kolibree.android.partnerships.data.persistence.PartnershipDao
import com.kolibree.android.partnerships.data.persistence.PartnershipPersistenceMapper
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.headspace.data.api.HeadspaceApiMapper
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspaceDatabase
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspacePartnershipDao
import com.kolibree.android.partnerships.headspace.data.persistence.HeadspacePersistenceMapper
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet

@Module
internal object HeadspacePartnershipImplementationModule {

    @Provides
    @IntoMap
    @PartnerKey(Partner.HEADSPACE)
    fun provideApiMapper(): PartnershipApiMapper = HeadspaceApiMapper

    @Provides
    @IntoMap
    @PartnerKey(Partner.HEADSPACE)
    fun providePersistenceMapper(): PartnershipPersistenceMapper = HeadspacePersistenceMapper

    @Provides
    @IntoMap
    @PartnerKey(Partner.HEADSPACE)
    fun provideDao(impl: HeadspacePartnershipDao): PartnershipDao = impl

    @Provides
    internal fun provideDaoImplementation(
        appDatabase: HeadspaceDatabase
    ): HeadspacePartnershipDao = appDatabase.headspacePartnershipDao()

    @IntoSet
    @Provides
    internal fun addDaoToTruncables(dao: HeadspacePartnershipDao): Truncable = dao
}

@Module
internal object HeadspacePartnershipDatabaseModule {

    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesHeadspaceDatabase(context: Context): HeadspaceDatabase {
        return Room.databaseBuilder(
            context,
            HeadspaceDatabase::class.java,
            HeadspaceDatabase.DATABASE_NAME
        ).addMigrations(*HeadspaceDatabase.migrations).build()
    }
}
