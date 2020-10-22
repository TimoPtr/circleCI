package com.kolibree.android.app.dagger

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.kolibree.BuildConfig
import com.kolibree.R
import com.kolibree.account.logout.IntentAfterForcedLogout
import com.kolibree.account.logout.IntentAfterForcedLogout.Companion.create
import com.kolibree.android.app.AppConfigurationImpl
import com.kolibree.android.app.async.AppClearUserContentJobService
import com.kolibree.android.app.crashlogger.CrashLogger
import com.kolibree.android.app.offlinebrushings.OfflineBrushingsResourceProviderImpl
import com.kolibree.android.app.toothbrush.FlavorToothbrushModels
import com.kolibree.android.app.tracker.FirebaseAnalyticsTracker
import com.kolibree.android.app.ui.cache.PicassoAvatarCacheWarmUp
import com.kolibree.android.app.ui.kolibree_pro.KolibreeProReminders
import com.kolibree.android.app.ui.kolibree_pro.NoOpKolibreeProReminders
import com.kolibree.android.app.ui.onboarding.OnboardingActivity
import com.kolibree.android.app.ui.pairing.usecases.SupportedToothbrushModels
import com.kolibree.android.app.ui.selectavatar.FileProviderAuthority
import com.kolibree.android.app.ui.settings.secret.persistence.AppSessionFlags
import com.kolibree.android.app.ui.welcome.ExecutePresyncInstallationFlagsUseCase
import com.kolibree.android.app.ui.welcome.ExecutePresyncInstallationFlagsUseCaseImpl
import com.kolibree.android.app.unity.GameSplashResourcesProviderImpl
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.logging.KLTimberTree
import com.kolibree.android.network.retrofit.DeviceParameters
import com.kolibree.android.network.retrofit.DeviceParameters.Companion.create
import com.kolibree.android.offlinebrushings.OfflineBrushingsResourceProvider
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.shop.data.configuration.ShopifyProductTag
import com.kolibree.android.tracker.logic.AnalyticsTracker
import com.kolibree.android.unity.GameSplashResourcesProvider
import com.kolibree.android.utils.EmailVerifier
import com.kolibree.android.utils.EmailVerifierImpl
import com.kolibree.sdkws.appdata.AppDataSyncEnabled
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.core.AvatarCacheWarmUp
import com.kolibree.sdkws.core.LocalAvatarCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.Locale

/** Created by miguelaragues on 7/9/17.  */
@Module
internal abstract class AppModule {
    @Binds
    abstract fun bindsExecuteInstallationFlagsUseCase(
        impl: ExecutePresyncInstallationFlagsUseCaseImpl
    ): ExecutePresyncInstallationFlagsUseCase

    @Binds
    abstract fun bindAnalyticsTracker(tracker: FirebaseAnalyticsTracker): AnalyticsTracker

    @Binds
    abstract fun bindSessionFlags(impl: AppSessionFlags): SessionFlags

    @Binds
    abstract fun bindsAvatarCache(impl: LocalAvatarCache): AvatarCache

    @Binds
    abstract fun bindsAvatarCacheWarmUp(impl: PicassoAvatarCacheWarmUp): AvatarCacheWarmUp

    @Binds
    abstract fun providesEmailVerifier(verifier: EmailVerifierImpl): EmailVerifier

    @Binds
    abstract fun bindsGameSplashProvider(
        impl: GameSplashResourcesProviderImpl
    ): GameSplashResourcesProvider

    companion object {

        @Provides
        fun provideDeviceParameter(): DeviceParameters = create(
            BuildConfig.VERSION_NAME.replace(
                BuildConfig.VERSION_NAME_SUFFIX,
                ""
            ),
            BuildConfig.VERSION_CODE_OVERRIDE
        )

        @Provides
        fun provideShopifyProductTag(): ShopifyProductTag = ShopifyProductTag("humproductapp")

        @Provides
        fun providesContext(application: Application): Context = application

        @Provides
        @AppScope
        fun providesTree(): KLTimberTree {
            return KLTimberTree.create(debug = !BuildConfig.RELEASE)
        }

        @Provides
        fun providesJobScheduler(context: Context): JobScheduler =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        @Provides
        fun providesSharedPreferences(context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

        @Provides
        fun providesLocale(): Locale = Locale.getDefault()

        @Provides
        fun providesClearUserContentJobInfo(context: Context): JobInfo =
            AppClearUserContentJobService.createBuilder(context).build()

        @Provides
        fun providesAfterLogoutIntent(context: Context): IntentAfterForcedLogout =
            create(context, OnboardingActivity::class.java)

        @Provides
        fun provideOfflineBrushingsResourceProvider(): OfflineBrushingsResourceProvider =
            OfflineBrushingsResourceProviderImpl

        @Provides
        @AppDataSyncEnabled
        fun providesIsAppDataSyncEnabled(): Boolean = false

        @Provides
        fun providesKolibreeProReminders(): KolibreeProReminders = NoOpKolibreeProReminders

        @Provides
        @FileProviderAuthority
        fun providesFileProviderAuthority(context: Context): String =
            context.getString(R.string.file_provider_authority)

        @Provides
        fun providesAppConfiguration(): AppConfiguration = AppConfigurationImpl

        @Provides
        @SupportedToothbrushModels
        fun providesSupportedToothbrushModels(): Set<ToothbrushModel> =
            FlavorToothbrushModels.defaultSupportedModels()

        @Provides
        fun providesExceptionLogger(): ExceptionLogger = CrashLogger
    }
}
