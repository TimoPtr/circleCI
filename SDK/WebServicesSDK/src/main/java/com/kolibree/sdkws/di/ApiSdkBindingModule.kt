/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */
package com.kolibree.sdkws.di

import com.kolibree.sdkws.core.SynchronizerJobService
import com.kolibree.sdkws.core.avro.AvroUploaderJobService
import dagger.Module
import dagger.android.ContributesAndroidInjector

/** Created by miguelaragues on 6/3/18.  */
@Module
abstract class ApiSdkBindingModule {

    @ContributesAndroidInjector
    abstract fun bindAvroUploaderJobService(): AvroUploaderJobService

    @ContributesAndroidInjector
    abstract fun bindSynchronizerJobService(): SynchronizerJobService
}
