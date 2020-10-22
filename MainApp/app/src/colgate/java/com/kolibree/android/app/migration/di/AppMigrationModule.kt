/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration.di

import com.kolibree.android.app.migration.UnpairMultiToothbrushPerProfileMigration
import com.kolibree.android.migration.Migration
import com.kolibree.android.migration.Migrations
import dagger.Binds
import dagger.Module
import dagger.Provides

/**
 * Migration module for Colgate Connect
 */
@Module(includes = [MigrationModule::class])
abstract class AppMigrationModule {

    @Binds
    abstract fun bindsUnpairMultiToothbrushMigration(impl: UnpairMultiToothbrushPerProfileMigration):
        Migration

    companion object {
        @Provides
        fun providesOrderedMigrationSet(
            unpairMultiToothbrushPerProfileMigration: UnpairMultiToothbrushPerProfileMigration
        ): Migrations {
            return listOf(
                unpairMultiToothbrushPerProfileMigration
            )
        }
    }
}
