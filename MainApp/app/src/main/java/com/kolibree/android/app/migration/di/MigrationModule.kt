/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.migration.di

import com.kolibree.android.app.migration.MigrationUseCase
import com.kolibree.android.app.migration.MigrationUseCaseImpl
import com.kolibree.android.migration.MigrationProvider
import com.kolibree.android.migration.MigrationProviderImpl
import dagger.Binds
import dagger.Module

@Module
internal abstract class MigrationModule {
    @Binds
    abstract fun bindsMigrationUseCase(impl: MigrationUseCaseImpl): MigrationUseCase

    @Binds
    abstract fun bindsMigrationProvider(impl: MigrationProviderImpl): MigrationProvider
}
