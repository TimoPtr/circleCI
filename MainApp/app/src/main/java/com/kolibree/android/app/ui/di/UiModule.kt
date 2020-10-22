/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.di

import com.kolibree.android.angleandspeed.ui.mindyourspeed.di.MindYourSpeedModule
import com.kolibree.android.app.base.createNavigatorAndBindToLifecycle
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.launcher.LauncherBindingModule
import com.kolibree.android.app.navigation.NavigationHelper
import com.kolibree.android.app.ui.addprofile.AddProfileBindingModule
import com.kolibree.android.app.ui.brushhead.di.BrushHeadModule
import com.kolibree.android.app.ui.celebration.EarnPointsCelebrationBindingModule
import com.kolibree.android.app.ui.checkup.CheckupModule
import com.kolibree.android.app.ui.game.StartNonUnityGameModule
import com.kolibree.android.app.ui.home.HomeNavigatorViewModel
import com.kolibree.android.app.ui.home.HomeScreenActivity
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.di.HomeScreenActivityBindingModule
import com.kolibree.android.app.ui.home.di.HomeScreenDisplayPriorityModule
import com.kolibree.android.app.ui.home.guidedbrushing.startscreen.GuidedBrushingStartScreenBindingModule
import com.kolibree.android.app.ui.home.mindyourspeed.startscreen.MindYourSpeedStartScreenBindingModule
import com.kolibree.android.app.ui.home.pairing.ToothbrushPairingBindingModule
import com.kolibree.android.app.ui.home.pairing.startscreen.PairingStartScreenBindingModule
import com.kolibree.android.app.ui.home.tab.home.card.brushingstreak.completion.BrushingStreakCompletionBindingModule
import com.kolibree.android.app.ui.home.tab.profile.completeprofile.CompleteProfileBubbleModule
import com.kolibree.android.app.ui.home.testbrushing.startscreen.TestBrushingStartScreenBindingModule
import com.kolibree.android.app.ui.navigation.HomeNavigator
import com.kolibree.android.app.ui.onboarding.di.OnboardingModule
import com.kolibree.android.app.ui.selectprofile.di.SelectProfileModule
import com.kolibree.android.app.ui.settings.about.di.AboutBindingModule
import com.kolibree.android.app.ui.settings.di.SettingsBindingModule
import com.kolibree.android.app.ui.settings.help.HelpBindingModule
import com.kolibree.android.app.ui.settings.notifications.NotificationsBindingModule
import com.kolibree.android.app.ui.settings.secret.SecretSettingsFactoryModule
import com.kolibree.android.app.ui.toothbrushsettings.di.ToothbrushSettingsBindingModule
import com.kolibree.android.guidedbrushing.settings.GuidedBrushingSettingsBindingModule
import com.kolibree.android.headspace.mindful.HeadspaceMindfulMomentNavigator
import com.kolibree.android.jaws.hum.HumJawsModule
import com.kolibree.android.rewards.morewaystoearnpoints.di.MoreWaysToEarnPointsModule
import com.kolibree.android.rewards.personalchallenge.di.HumChallengeModule
import com.kolibree.android.testbrushing.di.TestBrushingModule
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module(
    includes = [
        LauncherBindingModule::class,
        OnboardingModule::class,
        SettingsBindingModule::class,
        AboutBindingModule::class,
        HelpBindingModule::class,
        TestBrushingModule::class,
        MindYourSpeedModule::class,
        SecretSettingsFactoryModule::class,
        GuidedBrushingStartScreenBindingModule::class,
        TestBrushingStartScreenBindingModule::class,
        MindYourSpeedStartScreenBindingModule::class,
        HumJawsModule::class,
        CheckupModule::class,
        GuidedBrushingSettingsBindingModule::class,
        ToothbrushSettingsBindingModule::class,
        ToothbrushPairingBindingModule::class,
        PairingStartScreenBindingModule::class,
        BrushHeadModule::class,
        BrushingStreakCompletionBindingModule::class,
        NotificationsBindingModule::class,
        MoreWaysToEarnPointsModule::class,
        EarnPointsCelebrationBindingModule::class,
        CompleteProfileBubbleModule::class,
        AddProfileBindingModule::class
    ]
)
abstract class UiModule {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [
            HomeScreenActivityBindingModule::class,
            HomeScreenModule::class,
            HomeScrenActivityNavigatorModule::class,
            HumChallengeModule::class,
            HomeScreenDisplayPriorityModule::class,
            StartNonUnityGameModule::class,
            SelectProfileModule::class
        ]
    )
    internal abstract fun bindHumHomeScreenActivity(): HomeScreenActivity
}

@Module
internal object HomeScrenActivityNavigatorModule {

    @Provides
    fun providesHumHomeNavigator(
        activity: HomeScreenActivity,
        factory: HomeNavigatorViewModel.Factory
    ): HumHomeNavigator {
        return activity.createNavigatorAndBindToLifecycle(HomeNavigatorViewModel::class) { factory }
    }

    /*
    needed by StartNonUnityGameUseCaseImpl

    Will return same instance as HumHomeNavigator
     */
    @Provides
    fun providesHomeNavigator(
        activity: HomeScreenActivity,
        factory: HomeNavigatorViewModel.Factory
    ): HomeNavigator {
        return activity.createNavigatorAndBindToLifecycle(HomeNavigatorViewModel::class) {
            factory
        }
    }

    /*
    Helper for cards to perform generic actions
     */
    @Provides
    fun providesNavigationHelper(humHomeNavigator: HumHomeNavigator): NavigationHelper {
        return humHomeNavigator
    }

    /**
     * Helper for HeadspaceMindfulMoment action
     */
    @Provides
    fun providesHeadspaceMindfulMomentNavigator(humHomeNavigator: HumHomeNavigator): HeadspaceMindfulMomentNavigator =
        humHomeNavigator
}
