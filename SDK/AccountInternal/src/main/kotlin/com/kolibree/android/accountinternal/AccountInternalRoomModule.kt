/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.accountinternal

import android.content.Context
import androidx.room.Room
import com.kolibree.android.accountinternal.AccountRoomDatabase.Companion.DATABASE_NAME
import com.kolibree.android.accountinternal.persistence.dao.AccountDao
import com.kolibree.android.accountinternal.profile.persistence.dao.ProfileDao
import com.kolibree.android.app.dagger.AppScope
import dagger.Module
import dagger.Provides

@Module
internal object AccountInternalRoomModule {

    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    internal fun providesAppDatabase(context: Context): AccountRoomDatabase =
        Room.databaseBuilder(context, AccountRoomDatabase::class.java, DATABASE_NAME)
            .addMigrations(*AccountRoomDatabase.migrations)
            .build()

    @Provides
    internal fun providesProfileDao(appDatabase: AccountRoomDatabase): ProfileDao =
        appDatabase.profileDao()

    @Provides
    internal fun providesAccountDao(appDatabase: AccountRoomDatabase): AccountDao =
        appDatabase.accountDao()
}
