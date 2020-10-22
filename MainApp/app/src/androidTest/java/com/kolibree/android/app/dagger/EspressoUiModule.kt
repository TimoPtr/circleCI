/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.offlinebrushings.OfflineBrushingsResourceProviderImpl
import com.kolibree.android.app.ui.addprofile.AddProfileBindingModule
import com.kolibree.android.app.ui.celebration.EarnPointsCelebrationBindingModule
import com.kolibree.android.app.ui.checkup.CheckupModule
import com.kolibree.android.app.ui.di.HomeScreenSharedLogicModule
import com.kolibree.android.app.ui.di.HomeScrenActivityNavigatorModule
import com.kolibree.android.app.ui.game.StartNonUnityGameModule
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.app.ui.home.di.HomeScreenActivityBindingModule
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenBindingModule
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.MindYourSpeedStartScreenBindingModule
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenBindingModule
import com.kolibree.android.app.ui.home.pulsingdot.di.PulsingDotModule
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion.BrushingStreakCompletionBindingModule
import com.kolibree.android.app.ui.home.tab.profile.completeprofile.CompleteProfileBubbleModule
import com.kolibree.android.app.ui.home.testbrushing.startscreen.TestBrushingStartScreenBindingModule
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.ota.OtaUpdateBindingModule
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCase
import com.kolibree.android.app.ui.priority.AsyncDisplayItemUseCaseFactory
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.priority.DisplayPriorityUseCaseFactory
import com.kolibree.android.app.ui.selectprofile.di.SelectProfileModule
import com.kolibree.android.app.ui.settings.about.di.AboutBindingModule
import com.kolibree.android.app.ui.settings.di.SettingsBindingModule
import com.kolibree.android.app.ui.settings.help.HelpBindingModule
import com.kolibree.android.app.ui.settings.secret.SecretSettingsFactoryModule
import com.kolibree.android.app.ui.toothbrushsettings.di.ToothbrushSettingsBindingModule
import com.kolibree.android.guidedbrushing.settings.GuidedBrushingSettingsBindingModule
import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.rewards.smileshistory.SmilesHistoryBindingModule
import com.kolibree.android.test.dagger.EspressoHomeScreenModule
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.KLQueue
import com.kolibree.android.utils.KLQueueFactory
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import org.threeten.bp.Duration

@Module(
    includes = [
        SettingsBindingModule::class,
        TestBrushingStartScreenBindingModule::class,
        GuidedBrushingStartScreenBindingModule::class,
        MindYourSpeedStartScreenBindingModule::class,
        EspressoGuidedBrushingModule::class,
        EspressoTestBrushingModule::class,
        EspressoMindYourSpeedModule::class,
        AboutBindingModule::class,
        HelpBindingModule::class,
        SecretSettingsFactoryModule::class,
        CheckupModule::class,
        GuidedBrushingSettingsBindingModule::class,
        ToothbrushSettingsBindingModule::class,
        PairingStartScreenBindingModule::class,
        EspressoBrushHeadModule::class,
        PulsingDotModule::class,
        OtaUpdateBindingModule::class,
        BrushingStreakCompletionBindingModule::class,
        SmilesHistoryBindingModule::class,
        CompleteProfileBubbleModule::class,
        EarnPointsCelebrationBindingModule::class,
        EspressoNotificationsBindingModule::class,
        AddProfileBindingModule::class
    ]
)
abstract class EspressoUiModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            HomeScreenSharedLogicModule::class,
            StartNonUnityGameModule::class,
            EspressoHomeScreenModule::class,
            HomeScreenActivityBindingModule::class,
            HomeScrenActivityNavigatorModule::class,
            EspressoHomeScreenDisplayPriorityModule::class,
            SelectProfileModule::class
        ]
    )
    internal abstract fun bindHumHomeScreenActivity(): HomeScreenActivity

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            EspressoOnboardingActivityModule::class
        ]
    )
    internal abstract fun bindOnboardingActivity(): OnboardingActivity

    companion object {

        @Provides
        fun provideOfflineBrushingsResourceProvider(): OfflineBrushingsResourceProvider =
            OfflineBrushingsResourceProviderImpl
    }
}

@Module
class EspressoHomeScreenDisplayPriorityModule {

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    private annotation class EspressoHomeQueue

    @Provides
    @EspressoHomeQueue
    fun provideQueue(@SingleThreadScheduler scheduler: Scheduler): KLQueue {
        return EspressoKLQueue(
            KLQueueFactory.creates(
                delayScheduler = scheduler,
                delayAfterConsumption = Duration.ZERO
            )
        )
    }

    @Provides
    @ActivityScope
    fun provideDisplayPriorityUseCase(
        @EspressoHomeQueue queue: KLQueue
    ): DisplayPriorityItemUseCase<HomeDisplayPriority> {
        return DisplayPriorityUseCaseFactory.create(queue)
    }

    @Provides
    @ActivityScope
    fun provideOfflineBrushingAsyncDisplayUseCase(
        @EspressoHomeQueue queue: KLQueue
    ): AsyncDisplayItemUseCase<HomeDisplayPriority.OfflineBrushing> {
        return AsyncDisplayItemUseCaseFactory.create(queue)
    }
}

/**
 * Wrapper around default KLQueue implementation that unblocks the submit/stream operation
 *
 * KLQueueImpl adds an rx delay after submit that was blocking the UI
 */
private class EspressoKLQueue constructor(private val queue: KLQueue) : KLQueue by queue {
    override fun submit(item: KLItem) {
        queue.submit(item)
            .also {
                EspressoSingleThreadSchedulerModule.scheduler.advanceTimeBy(
                    1,
                    TimeUnit.MILLISECONDS
                )
            }
    }
}
