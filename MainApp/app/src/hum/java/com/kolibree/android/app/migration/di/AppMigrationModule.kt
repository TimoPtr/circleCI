/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration.di

import com.kolibree.android.migration.Migrations
import dagger.Module
import dagger.Provides

/**
 * Migration module for Hum
 */
@Module(includes = [MigrationModule::class])
class AppMigrationModule {

    @Provides
    fun providesOrderedMigrationSet(): Migrations {
        return emptyList()
    }
}
