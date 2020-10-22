/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.di

import android.content.Context
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.app.dagger.BaseUIModule
import com.kolibree.android.app.dagger.CommonsAndroidModule
import com.kolibree.android.glimmer.GlimmerApplication
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.synchronizator.SynchronizatorModule
import com.kolibree.pairing.PairingModule
import com.kolibree.statsoffline.StatsOfflineModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@Component(
    dependencies = [SdkComponent::class],
    modules = [
        AndroidSupportInjectionModule::class,
        ConnectionModule::class,
        KolibreeModule::class,
        GlimmerTweakerApplicationModule::class,
        GlimmerTweakerApiSDKModule::class,
        ProcessedBrushingsModule::class,
        SynchronizatorModule::class,
        PairingModule::class,
        BaseUIModule::class,
        CommonsAndroidModule::class,
        StatsOfflineModule::class
    ]
)
@AppScope
interface GlimmerTweakerApplicationComponent : AndroidInjector<GlimmerApplication> {
    fun withConnectionComponent(): WithConnectionComponent.Factory

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        @BindsInstance
        fun app(app: GlimmerApplication): Builder

        fun kolibreeSdk(sdkComponent: SdkComponent): Builder

        fun build(): GlimmerTweakerApplicationComponent
    }
}
