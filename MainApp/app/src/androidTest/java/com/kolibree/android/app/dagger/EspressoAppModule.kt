/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.dagger

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import com.kolibree.BuildConfig
import com.kolibree.R
import com.kolibree.android.app.AppConfigurationImpl
import com.kolibree.android.app.async.AppClearUserContentJobService
import com.kolibree.android.app.location.LocationActionChecker
import com.kolibree.android.app.toothbrush.FlavorToothbrushModels
import com.kolibree.android.app.ui.pairing.usecases.SupportedToothbrushModels
import com.kolibree.android.app.ui.selectavatar.FileProviderAuthority
import com.kolibree.android.app.ui.settings.secret.persistence.AppSessionFlags
import com.kolibree.android.app.ui.welcome.ExecutePresyncInstallationFlagsUseCase
import com.kolibree.android.app.ui.welcome.ExecutePresyncInstallationFlagsUseCase.Companion.noOp
import com.kolibree.android.app.utils.AvatarDataStore
import com.kolibree.android.app.utils.AvatarUtils
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.commons.ExceptionLogger
import com.kolibree.android.commons.NoOpExceptionLogger
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.logging.KLTimberTree
import com.kolibree.android.network.retrofit.DeviceParameters
import com.kolibree.android.network.retrofit.DeviceParameters.Companion.create
import com.kolibree.android.persistence.SessionFlags
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.test.FakePaparazzi
import com.kolibree.android.test.Paparazzi
import com.kolibree.android.test.RealPaparazzi
import com.kolibree.android.tracker.logic.AnalyticsTracker
import com.kolibree.android.tracker.logic.NoOpAnalyticsTracker
import com.kolibree.sdkws.appdata.AppDataSyncEnabled
import com.nhaarman.mockitokotlin2.mock
import dagger.Binds
import dagger.Module
import dagger.Provides
import java.util.Locale
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

@Module(includes = [AvatarUtilsModule::class])
object EspressoAppModule {

    @Provides
    fun provideContext(application: Application): Context {
        return application
    }

    @Provides
    @AppScope
    fun providesTree(): KLTimberTree {
        return KLTimberTree.create(debug = true)
    }

    @Provides
    @AppScope
    fun providesJobScheduler(): JobScheduler {
        return Mockito.mock(JobScheduler::class.java)
    }

    @Provides
    @AppScope
    fun providesKolibreeService(): KolibreeService {
        return Mockito.mock(KolibreeService::class.java)
    }

    @Provides
    @AppScope
    fun providesSharedPreferences(editor: SharedPreferences.Editor): SharedPreferences {
        val sharedPrefs =
            Mockito.mock(SharedPreferences::class.java)
        Mockito.`when`(sharedPrefs.edit()).thenReturn(editor)
        Mockito.`when`(
            sharedPrefs.edit().putString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        ).thenReturn(editor)
        return sharedPrefs
    }

    @Provides
    @AppScope
    fun providesEditor(): SharedPreferences.Editor {
        val editor = Mockito.mock(
            SharedPreferences.Editor::class.java
        )
        Mockito.`when`(
            editor.putBoolean(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyBoolean()
            )
        ).thenReturn(editor)
        Mockito.`when`(
            editor.putLong(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyLong()
            )
        ).thenReturn(editor)
        Mockito.`when`(editor.remove(ArgumentMatchers.anyString()))
            .thenReturn(editor)
        return editor
    }

    @Provides
    fun provideDeviceParameter(): DeviceParameters {
        return create(
            BuildConfig.VERSION_NAME.replace(
                BuildConfig.VERSION_NAME_SUFFIX,
                ""
            ),
            BuildConfig.VERSION_CODE_OVERRIDE
        )
    }

    @Provides
    fun providesLocale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun providesClearUserContentJobInfo(context: Context): JobInfo {
        return AppClearUserContentJobService.createBuilder(context).build()
    }

    @Provides
    @AppDataSyncEnabled
    fun providesIsAppDataSyncEnabled(): Boolean {
        return false
    }

    @Provides
    fun providesExecuteInstallationFlagsUseCase(): ExecutePresyncInstallationFlagsUseCase {
        return noOp()
    }

    @Provides
    fun providesAnalyticsTracker(): AnalyticsTracker {
        return NoOpAnalyticsTracker()
    }

    @Provides
    fun providesSessionFlags(context: Context): SessionFlags {
        return AppSessionFlags(context)
    }

    @Provides
    @AppScope
    fun providesLocationForKLTBConnections(): LocationActionChecker {
        return mock()
    }

    @Provides
    @FileProviderAuthority
    fun providesFileProviderAuthority(context: Context): String =
        context.getString(R.string.file_provider_authority)

    @Provides
    fun providesAppConfiguration(): AppConfiguration = AppConfigurationImpl

    @Provides
    fun providesPaparazzi(): Paparazzi =
        @Suppress("ConstantConditionIf")
        if (BuildConfig.ENABLE_SCREENSHOT_TESTING) RealPaparazzi
        else FakePaparazzi

    @Provides
    @SupportedToothbrushModels
    fun providesSupportedToothbrushModels(): Set<ToothbrushModel> =
        FlavorToothbrushModels.defaultSupportedModels()

    @Provides
    fun providesExceptionLogger(): ExceptionLogger = NoOpExceptionLogger
}

@Module
internal abstract class AvatarUtilsModule {

    @Binds
    internal abstract fun bindsAvatarUtils(impl: AvatarUtils): AvatarDataStore
}
