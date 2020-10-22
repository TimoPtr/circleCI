/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.partnerships.data.di

import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.feature.FeatureToggleSet
import com.kolibree.android.feature.HeadspaceFeature
import com.kolibree.android.feature.toggleIsOn
import com.kolibree.android.partnerships.data.PartnershipStatusRepository
import com.kolibree.android.partnerships.data.PartnershipStatusRepositoryImpl
import com.kolibree.android.partnerships.data.api.PartnershipApi
import com.kolibree.android.partnerships.data.api.PartnershipApiFake
import com.kolibree.android.partnerships.domain.DisablePartnershipUseCase
import com.kolibree.android.partnerships.domain.DisablePartnershipUseCaseImpl
import com.kolibree.android.partnerships.domain.NoOpPartnershipStatusUseCase
import com.kolibree.android.partnerships.domain.PartnershipStatusUseCase
import com.kolibree.android.partnerships.domain.PartnershipStatusUseCaseImpl
import com.kolibree.android.partnerships.domain.UnlockPartnershipUseCase
import com.kolibree.android.partnerships.domain.UnlockPartnershipUseCaseImpl
import com.kolibree.android.partnerships.headspace.data.di.HeadspacePartnershipDatabaseModule
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module(
    includes = [
        PartnershipApiModule::class,
        PartnershipLogicModule::class,
        PartnershipImplementationsModule::class,
        HeadspacePartnershipDatabaseModule::class
    ]
)
object PartnershipModule

@Module
internal object PartnershipApiModule {

    @Provides
    fun provideApi(retrofit: Retrofit): PartnershipApi {
        // TODO uncomment once API is ready
        // return retrofit.create(PartnershipApi::class.java)
        return PartnershipApiFake
    }
}

@Module
@VisibleForApp
object PartnershipLogicModule {

    @Provides
    internal fun bindUnlockPartnershipUseCase(
        impl: UnlockPartnershipUseCaseImpl
    ): UnlockPartnershipUseCase = impl

    @Provides
    internal fun bindDisablePartnershipUseCase(
        impl: DisablePartnershipUseCaseImpl
    ): DisablePartnershipUseCase = impl

    @Provides
    internal fun bindRepository(
        impl: PartnershipStatusRepositoryImpl
    ): PartnershipStatusRepository = impl

    @Provides
    internal fun provideHeadspaceUseCase(
        impl: PartnershipStatusUseCaseImpl,
        appConfiguration: AppConfiguration,
        featureToggleSet: FeatureToggleSet
    ): PartnershipStatusUseCase =
        if (appConfiguration.showHeadspaceRelatedContent &&
            featureToggleSet.toggleIsOn(HeadspaceFeature)
        ) impl
        else {
            // Headspace is not supported by this configuration.
            // We'll use no-op implementation to satisfy the interface,
            // while skipping injection of the real implementation.
            NoOpPartnershipStatusUseCase
        }
}
