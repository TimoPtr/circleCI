package com.kolibree.android.app.dagger

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.databinding.DataBindingComponent
import com.kolibree.account.di.AccountModule
import com.kolibree.account.profile.ProfileDeletedHook
import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.amazondash.di.AmazonDashModule
import com.kolibree.android.app.AppInitializerModule
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.app.job.di.WorkerModule
import com.kolibree.android.app.migration.di.AppMigrationModule
import com.kolibree.android.app.push.PushNotificationService
import com.kolibree.android.app.push.di.PushNotificationModule
import com.kolibree.android.app.sdkwrapper.KolibreeFacade
import com.kolibree.android.app.sdkwrapper.KolibreeModule
import com.kolibree.android.app.ui.chart.ChartPlaygroundBindingModule
import com.kolibree.android.app.ui.databinding.di.DataBindingAdapterModule
import com.kolibree.android.app.ui.di.UiModule
import com.kolibree.android.app.ui.notification.NotificationPresenterModule
import com.kolibree.android.app.ui.ota.OtaUpdateBindingModule
import com.kolibree.android.app.ui.settings.secret.persistence.ModelsAvailableModule
import com.kolibree.android.app.unity.UnityGameModule
import com.kolibree.android.brushingquiz.di.BrushingProgramModule
import com.kolibree.android.brushreminder.di.BrushReminderModule
import com.kolibree.android.brushsyncreminder.BrushSyncReminderModule
import com.kolibree.android.commons.AnimationInfoProviderModule
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.game.GameModule
import com.kolibree.android.guidedbrushing.di.GuidedBrushingModule
import com.kolibree.android.guidedbrushing.di.GuidedBrushingTipsModule
import com.kolibree.android.headspace.mindful.di.HeadspaceMindfulMomentScreenModule
import com.kolibree.android.jaws.JawsModule
import com.kolibree.android.jaws.coach.AndroidConfigModule
import com.kolibree.android.jaws.coach.CoachPlusView
import com.kolibree.android.jaws.color.ColorJawsView
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsModule
import com.kolibree.android.jaws.guidedbrushing.GuidedBrushingJawsView
import com.kolibree.android.jaws.hum.HumJawsModule
import com.kolibree.android.jaws.hum.HumJawsView
import com.kolibree.android.offlinebrushings.di.OfflineBrushingsModule
import com.kolibree.android.partnerships.data.di.PartnershipModule
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.questionoftheday.di.QuestionOfTheDayModule
import com.kolibree.android.rewards.RewardsModule
import com.kolibree.android.rewards.personalchallenge.di.PersonalChallengeModule
import com.kolibree.android.rewards.smileshistory.SmilesHistoryBindingModule
import com.kolibree.android.sdk.dagger.SdkComponent
import com.kolibree.android.sdk.util.IBluetoothUtils
import com.kolibree.android.shop.data.di.ShopDataModule
import com.kolibree.android.shop.di.ShopifyCredentialsModule
import com.kolibree.android.synchronizator.SynchronizatorModule
import com.kolibree.android.synchronizator.models.BundleCreator
import com.kolibree.android.toothbrush.battery.di.BatteryLevelModule
import com.kolibree.android.tracker.EventTracker
import com.kolibree.android.tracker.di.EventTrackerModule
import com.kolibree.charts.di.StatsModule
import com.kolibree.databinding.playground.lottie.LottiePlaygroundBindingModule
import com.kolibree.pairing.PairingModule
import com.kolibree.sdkws.calendar.di.CalendarLogicModule
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.di.ApiSDKModule
import com.kolibree.statsoffline.StatsOfflineModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import org.threeten.bp.Clock

@AppScope
@Component(
    dependencies = [SdkComponent::class],
    modules = [
        AndroidSupportInjectionModule::class,
        BindingModule::class,
        BuildTypeModule::class,
        FeatureToggleModule::class,
        AppModule::class,
        UiModule::class,
        KolibreeModule::class,
        StatsModule::class,
        PairingModule::class,
        ApiSDKModule::class,
        EventTrackerModule::class,
        JawsModule::class,
        OfflineBrushingsModule::class,
        ModelsAvailableModule::class,
        PairingModule::class,
        VariantModule::class,
        GuidedBrushingModule::class,
        ProcessedBrushingsModule::class,
        SynchronizatorModule::class,
        RewardsModule::class,
        SmilesHistoryBindingModule::class,
        BaseUIModule::class,
        UnityGameModule::class,
        CommonsAndroidModule::class,
        AndroidConfigModule::class,
        AnimationInfoProviderModule::class,
        StatsOfflineModule::class,
        CalendarLogicModule::class,
        AccountModule::class,
        BrushingProgramModule::class,
        PushNotificationModule::class,
        DataBindingAdapterModule::class,
        PersonalChallengeModule::class,
        ShopDataModule::class,
        ShopifyCredentialsModule::class,
        GameModule::class,
        LottiePlaygroundBindingModule::class,
        ChartPlaygroundBindingModule::class,
        HumJawsModule::class,
        GuidedBrushingJawsModule::class,
        OtaUpdateBindingModule::class,
        QuestionOfTheDayModule::class,
        WorkerModule::class,
        BrushSyncReminderModule::class,
        NotificationPresenterModule::class,
        SingleThreadSchedulerModule::class,
        AppInitializerModule::class,
        AmazonDashModule::class,
        BatteryLevelModule::class,
        BrushReminderModule::class,
        PartnershipModule::class,
        AppMigrationModule::class,
        GuidedBrushingTipsModule::class,
        HeadspaceMindfulMomentScreenModule::class
    ]
)
interface AppComponent : DataBindingComponent {
    fun context(): Context?
    fun utcClock(): Clock?
    fun kolibreeFacade(): KolibreeFacade
    fun kolibreeConnector(): IKolibreeConnector
    fun bluetoothUtils(): IBluetoothUtils
    fun eventTracker(): EventTracker
    fun inject(app: BaseKolibreeApplication)
    fun inject(pushNotificationService: PushNotificationService)
    fun inject(colorJawsView: ColorJawsView)
    fun inject(humJawsView: HumJawsView)
    fun inject(coachPlusView: CoachPlusView)
    fun inject(guidedBrushingJawsView: GuidedBrushingJawsView)

    @VisibleForTesting
    fun featureToggles(): Set<FeatureToggle<*>>

    @VisibleForTesting
    fun truncables(): Set<Truncable>

    @VisibleForTesting
    fun bundleCreators(): Set<BundleCreator>

    @VisibleForTesting
    fun toothbrushForgottenHooks(): Set<ToothbrushForgottenHook>

    @VisibleForTesting
    fun profileDeletedHooks(): Set<ProfileDeletedHook>

    @VisibleForTesting
    fun brushingsProcessor(): LocalBrushingsProcessor

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun context(application: Application): Builder
        fun sdkComponent(sdkComponent: SdkComponent): Builder
        fun build(): AppComponent
    }
}
