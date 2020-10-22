/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.core

import com.kolibree.android.app.dagger.AppScope
import com.kolibree.sdkws.appdata.AppDataModule
import com.kolibree.sdkws.core.avro.AvroFileUploader
import com.kolibree.sdkws.core.avro.AvroFileUploaderImpl
import dagger.Binds
import dagger.Module

/** Created by miguelaragues on 6/3/18.  */
@Module(includes = [AppDataModule::class])
abstract class CoreModule {
    @Binds
    @AppScope
    internal abstract fun bindsInternalKolibreeConnector(
        connector: KolibreeConnector
    ): InternalKolibreeConnector

    @Binds
    internal abstract fun bindsKolibreeConnector(
        kolibreeConnector: InternalKolibreeConnector
    ): IKolibreeConnector

    @Binds
    internal abstract fun bindsInternalForceAppUpdater(
        forceAppUpdateChecker: ForceAppUpdateCheckerImpl
    ): InternalForceAppUpdater

    @Binds
    internal abstract fun bindsForceAppUpdater(
        internalForceAppUpdater: InternalForceAppUpdater
    ): ForceAppUpdateChecker

    @Binds
    internal abstract fun bindsAvroUploader(impl: AvroFileUploaderImpl): AvroFileUploader

    @Binds
    internal abstract fun bindsSynchronizationScheduler(
        impl: SynchronizationSchedulerImpl
    ): SynchronizationScheduler
}
