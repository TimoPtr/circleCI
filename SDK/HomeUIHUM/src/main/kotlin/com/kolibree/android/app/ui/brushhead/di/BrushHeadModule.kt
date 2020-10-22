/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.di

import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.app.ui.brushhead.api.BrushHeadInformationApi
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadDateSendApiProvider
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadDateSendApiProviderImpl
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadInformationReader
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadReplaceDateManager
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadReplacedDateWriter
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepositoryImpl
import com.kolibree.android.app.ui.brushhead.sync.BrushHeadSynchronizationModule
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCase
import com.kolibree.android.app.ui.brushhead.usecase.BrushHeadConditionUseCaseImpl
import com.kolibree.android.app.ui.toothbrushsettings.worker.CancelReplaceBrushHeadWorkerHook
import com.kolibree.android.app.ui.toothbrushsettings.worker.CancelSyncBrushHeadWorkerHook
import com.kolibree.android.commons.interfaces.Truncable
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module(
    includes = [
        BrushHeadDomainModule::class,
        BrushHeadDataModule::class,
        BrushHeadApiModule::class,
        BrushHeadSynchronizationModule::class
    ]
)
abstract class BrushHeadModule {
    @Binds
    @IntoSet
    internal abstract fun bindsCancelReplaceBrushHeadWorkerHook(
        impl: CancelReplaceBrushHeadWorkerHook
    ): ToothbrushForgottenHook

    @Binds
    @IntoSet
    internal abstract fun bindsCancelSyncBrushHeadWorkerHook(
        impl: CancelSyncBrushHeadWorkerHook
    ): ToothbrushForgottenHook
}

@Module
abstract class BrushHeadDomainModule {

    @Binds
    internal abstract fun bindsBrushHeadReplacedDateWriter(
        implementation: BrushHeadReplaceDateManager
    ): BrushHeadReplacedDateWriter

    @Binds
    internal abstract fun bindsBrushHeadReplacedDateReader(
        implementation: BrushHeadReplaceDateManager
    ): BrushHeadInformationReader

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableBrushHeadReplaceDateManager(
        repository: BrushHeadReplaceDateManager
    ): Truncable

    @Binds
    internal abstract fun bindsBrushHeadConditionUseCase(
        implementation: BrushHeadConditionUseCaseImpl
    ): BrushHeadConditionUseCase
}

@Module
abstract class BrushHeadDataModule {
    @Binds
    internal abstract fun bindsBrushHeadRepository(
        implementation: BrushHeadRepositoryImpl
    ): BrushHeadRepository

    @Binds
    internal abstract fun bindsBrushHeadDateSendApiProvider(
        implementation: BrushHeadDateSendApiProviderImpl
    ): BrushHeadDateSendApiProvider
}

@Module
class BrushHeadApiModule {
    @Provides
    internal fun providesReplaceBrushHeadApi(retrofit: Retrofit): BrushHeadInformationApi {
        return retrofit.create(BrushHeadInformationApi::class.java)
    }
}
