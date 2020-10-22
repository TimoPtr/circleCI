/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.sync.job

import android.content.Context
import android.content.Intent
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.OfflineBrushingsNotificationsFeature
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.offlinebrushings.di.ExtractOfflineBrushingsModule
import com.kolibree.android.offlinebrushings.di.OfflineNotificationIntent
import com.kolibree.android.rewards.feedback.RewardsFeedbackDomainModule
import com.kolibree.android.sdk.core.BackgroundJobManager
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoSet

@Module(includes = [ToothbrushScanJobServiceModule::class])
abstract class NightsWatchOfflineBrushingsCheckerModule {

    @ContributesAndroidInjector(
        modules = [
            ExtractOfflineBrushingsModule::class,
            RewardsFeedbackDomainModule::class
        ]
    )
    internal abstract fun bindsNightsWatchOfflineBrushingsChecker(): NightsWatchOfflineBrushingsChecker

    @BindsOptionalOf
    @OfflineNotificationIntent
    internal abstract fun bindOfflineNotificationIntent(): Intent

    @ContributesAndroidInjector
    internal abstract fun bindScheduleToothbrushScanJobService(): ToothbrushScanJobService

    @ContributesAndroidInjector
    internal abstract fun bindToothbrushScannedBroadcastReceiver(): ToothbrushScannedBroadcastReceiver

    @ContributesAndroidInjector
    internal abstract fun bindNightsWatchBootBroadcastReceiver(): NightsWatchBootBroadcastReceiver

    @Binds
    internal abstract fun bindsNightsWatchScanner(impl: NightsWatchScannerImpl): NightsWatchScanner

    @Binds
    internal abstract fun bindsNightsWatchCheckerSchedulerImpl(impl: NightsWatchSchedulerImpl): NightsWatchScheduler

    @Binds
    internal abstract fun bindsNightsWatchCheckerScheduler(impl: NightsWatchScheduler): BackgroundJobManager
}

@Module
class NightsWatchOfflineBrushingsCheckerToggleModule {

    @Provides
    @IntoSet
    fun provideOfflineBrushingsNotificationsFeature(context: Context): FeatureToggle<*> {
        return PersistentFeatureToggle(context, OfflineBrushingsNotificationsFeature)
    }
}
