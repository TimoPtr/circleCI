package com.kolibree.core.dagger

import cn.colgate.colgateconnect.modules.AuthenticationFlowModule
import com.kolibree.account.di.AccountModule
import com.kolibree.android.angleandspeed.speedcontrol.di.SpeedControlModule
import com.kolibree.android.angleandspeed.testangles.di.TestAnglesModule
import com.kolibree.android.app.dagger.BaseUIModule
import com.kolibree.android.app.dagger.CommonsAndroidModule
import com.kolibree.android.app.dagger.ImageLoaderModule
import com.kolibree.android.app.dagger.SingleThreadSchedulerModule
import com.kolibree.android.app.unity.UnityGameModule
import com.kolibree.android.brushingquiz.di.BrushingProgramModule
import com.kolibree.android.coachplus.di.CoachPlusModule
import com.kolibree.android.guidedbrushing.di.GuidedBrushingModule
import com.kolibree.android.jaws.JawsModule
import com.kolibree.android.jaws.coach.AndroidConfigModule
import com.kolibree.android.offlinebrushings.di.OfflineBrushingsModule
import com.kolibree.android.offlinebrushings.di.V1OfflineBrushingsModule
import com.kolibree.android.offlinebrushings.sync.job.NightsWatchOfflineBrushingsCheckerToggleModule
import com.kolibree.android.pirate.PirateModule
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.rewards.RewardsModule
import com.kolibree.android.rewards.personalchallenge.di.PersonalChallengeModule
import com.kolibree.android.synchronizator.SynchronizatorModule
import com.kolibree.android.toothbrushupdate.ToothbrushUpdateModule
import com.kolibree.charts.di.StatsModule
import com.kolibree.pairing.PairingModule
import com.kolibree.sdkws.di.ApiSDKModule
import com.kolibree.statsoffline.StatsOfflineModule
import dagger.Module
import dagger.android.support.AndroidSupportInjectionModule

// all the modules in the public android SDK
@Module(
    includes = [AndroidSupportInjectionModule::class, CoreDependenciesModule::class, // dep for the android SDK
        SingleThreadSchedulerModule::class, // dep needed for the offline brushing
        AuthenticationFlowModule::class, // dep for auth module
        StatsModule::class, // dep for stats module
        ApiSDKModule::class, // dep for core SDK module
        PairingModule::class, // dep for pairing module
        OfflineBrushingsModule::class, // dep for the offline brushing module
        V1OfflineBrushingsModule::class, // dep for the V1 offline brushing module
        NightsWatchOfflineBrushingsCheckerToggleModule::class, RewardsModule::class, // dep needed by nightswatch
        JawsModule::class, // dep for the jaws 3D module
        CoachPlusModule::class, // dep for the coach+ module
        GuidedBrushingModule::class, // dep for the new guided brushing ui
        UnityGameModule::class, AndroidConfigModule::class, PirateModule::class, // dep for the pirate game
        ToothbrushUpdateModule::class,
        AccountModule::class,
        ProcessedBrushingsModule::class,
        SynchronizatorModule::class,
        PersonalChallengeModule::class,
        CoreBindingModule::class,
        BaseUIModule::class,
        ImageLoaderModule::class,
        CommonsAndroidModule::class,
        StatsOfflineModule::class,
        BrushingProgramModule::class,
        TestAnglesModule::class,
        SpeedControlModule::class]
)
abstract class CoreModule
