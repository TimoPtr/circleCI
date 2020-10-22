/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.kolibree.android.amazondash.data.remote.AmazonDashApi
import com.kolibree.android.amazondash.domain.AmazonDashAvailabilityUseCase
import com.kolibree.android.amazondash.domain.AmazonDashAvailabilityUseCaseImpl
import com.kolibree.android.amazondash.domain.AmazonDashCheckAlexaUseCase
import com.kolibree.android.amazondash.domain.AmazonDashCheckAlexaUseCaseImpl
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCase
import com.kolibree.android.amazondash.domain.AmazonDashExtractTokenUseCaseImpl
import com.kolibree.android.amazondash.domain.AmazonDashLinkUseCase
import com.kolibree.android.amazondash.domain.AmazonDashLinkUseCaseImpl
import com.kolibree.android.amazondash.domain.AmazonDashSendTokenUseCase
import com.kolibree.android.amazondash.domain.AmazonDashSendTokenUseCaseImpl
import com.kolibree.android.amazondash.domain.AmazonDashVerifyStateUseCase
import com.kolibree.android.amazondash.domain.AmazonDashVerifyStateUseCaseImpl
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectActivity
import com.kolibree.android.amazondash.ui.connect.AmazonDashConnectNavigator
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.feature.AmazonDashFeature
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet
import retrofit2.Retrofit

@Module(
    includes = [
        AmazonDashCoreModule::class,
        AmazonDashNetworkModule::class
    ]
)
interface AmazonDashModule

/**
 * Includes all dependencies except networking.
 * Allows to provide fake implementation for Espresso.
 */
@Module(
    includes = [
        AmazonDashBindingModule::class,
        AmazonDashToggleModule::class,
        AmazonDashUseCaseModule::class
    ]
)
interface AmazonDashCoreModule

@Module
internal class AmazonDashNetworkModule {

    @Provides
    fun providesAmazonDashApi(retrofit: Retrofit): AmazonDashApi {
        return retrofit.create(AmazonDashApi::class.java)
    }
}

@Module
internal class AmazonDashToggleModule {

    @Provides
    @IntoSet
    fun provideAmazonDashToggle(context: Context): FeatureToggle<*> {
        return PersistentFeatureToggle(context, AmazonDashFeature)
    }
}

@Module
internal interface AmazonDashBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [AmazonDashConnectModule::class])
    fun bindAmazonDashConnectActivity(): AmazonDashConnectActivity
}

@Module
internal interface AmazonDashConnectModule {

    @Binds
    fun bindAppCompatActivity(activity: AmazonDashConnectActivity): AppCompatActivity

    companion object {

        @Provides
        fun providesNavigator(activity: AmazonDashConnectActivity): AmazonDashConnectNavigator {
            return activity.createNavigatorAndBindToLifecycle(AmazonDashConnectNavigator::class)
        }
    }
}

@Module
internal interface AmazonDashUseCaseModule {

    @Binds
    fun bindsAvailabilityUseCase(
        impl: AmazonDashAvailabilityUseCaseImpl
    ): AmazonDashAvailabilityUseCase

    @Binds
    fun bindsLinkUseCase(
        impl: AmazonDashLinkUseCaseImpl
    ): AmazonDashLinkUseCase

    @Binds
    fun bindsAlexaCheckUseCase(
        impl: AmazonDashCheckAlexaUseCaseImpl
    ): AmazonDashCheckAlexaUseCase

    @Binds
    fun bindsUploadUseCase(
        impl: AmazonDashSendTokenUseCaseImpl
    ): AmazonDashSendTokenUseCase

    @Binds
    fun bindsHandleResponseUseCase(
        impl: AmazonDashExtractTokenUseCaseImpl
    ): AmazonDashExtractTokenUseCase

    @Binds
    fun bindsVerifyStateUseCase(
        impl: AmazonDashVerifyStateUseCaseImpl
    ): AmazonDashVerifyStateUseCase
}
