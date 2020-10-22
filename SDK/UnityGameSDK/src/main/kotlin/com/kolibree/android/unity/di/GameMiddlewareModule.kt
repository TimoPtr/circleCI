/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.common.base.Optional
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.commons.DEFAULT_BRUSHING_GOAL
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.GameScope
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.ProcessedBrushingsModule
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.android.unity.GameMiddlewareActivity
import com.kolibree.android.unity.UnityGameFragment
import com.kolibree.android.unity.middleware.NativeSDKInstanceImpl
import com.kolibree.android.unity.middleware.ResourcesInteractorImpl
import com.kolibree.android.unity.middleware.ToothbrushInteractorImpl
import com.kolibree.android.unity.middleware.WebServicesInteractorImpl
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.game.middleware.NativeSDKInstance
import com.kolibree.game.middleware.ResourcesInteractor
import com.kolibree.game.middleware.ToothbrushInteractor
import com.kolibree.game.middleware.WebServicesInteractor
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.InternalKolibreeConnector
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import javax.inject.Named
import org.threeten.bp.Duration

@Module
internal abstract class ToothbrushInteractorModule {

    @Binds
    @FragmentScope
    abstract fun bindToothbrushInteractor(toothbrushInteractorImpl: ToothbrushInteractorImpl): ToothbrushInteractor
}

@Module(
    includes = [
        KpiSpeedProviderModule::class,
        RnnWeightProviderModule::class,
        ProcessedBrushingsModule::class,
        ToothbrushInteractorModule::class,
        AvroCreatorModule::class
    ]
)
@SuppressLint("ExperimentalClassUse")
internal class GameMiddlewareModule {

    @Provides
    @FragmentScope
    fun provideDefaultScheduler(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

    @Provides
    @FragmentScope
    fun provideNativeSdkInstance(
        resourcesInteractorProvider: ResourcesInteractor,
        toothbrushInteractor: ToothbrushInteractor,
        webServicesInteractor: WebServicesInteractor
    ): NativeSDKInstance =
        NativeSDKInstanceImpl(
            resourcesInteractorProvider,
            toothbrushInteractor,
            webServicesInteractor
        )

    @Provides
    @FragmentScope
    fun provideResourceInteractor(
        disposableScopeOwner: LifecycleDisposableScopeOwner,
        angleProvider: AngleProvider,
        speedProvider: KpiSpeedProvider?,
        transitionProvider: TransitionProvider,
        rnnWeightProvider: RnnWeightProvider?,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        kolibreeAppVersions: KolibreeAppVersions
    ): ResourcesInteractor {
        checkNotNull(speedProvider) {
            "KpiSpeedProvider should not be null did you provide the TB model?"
        }
        checkNotNull(rnnWeightProvider) {
            "RnnWeightProvider should not be null did you provide the TB model?"
        }
        return ResourcesInteractorImpl(
            disposableScopeOwner,
            angleProvider,
            speedProvider,
            transitionProvider,
            rnnWeightProvider,
            thresholdProvider,
            zoneValidatorProvider,
            kolibreeAppVersions
        )
    }

    @Provides
    @FragmentScope
    fun provideWebServicesInteractor(
        disposableScopeOwner: LifecycleDisposableScopeOwner,
        kolibreeConnector: IKolibreeConnector,
        gameProgressRepository: GameProgressRepository,
        checkupCalculator: CheckupCalculator,
        appVersions: KolibreeAppVersions,
        @Named(DI_GOAL_BRUSHING_TIME) goalBrushingTime: Duration,
        avroCreator: KmlAvroCreator
    ): WebServicesInteractor {
        val profile = kolibreeConnector.currentProfile
        checkNotNull(profile) { "Profile cannot be null!" }
        return WebServicesInteractorImpl(
            disposableScopeOwner,
            profile,
            kolibreeConnector,
            gameProgressRepository,
            checkupCalculator,
            appVersions,
            goalBrushingTime,
            avroCreator
        )
    }

    @Provides
    fun provideLifecycleDisposableScopeOwner(
        lifecycle: Lifecycle
    ): LifecycleDisposableScopeOwner =
        LifecycleDisposableScopeOwner(lifecycle)

    @Provides
    @FragmentScope
    internal fun provideKolibreeConnector(kolibreeConnector: InternalKolibreeConnector): IKolibreeConnector =
        kolibreeConnector

    @Provides
    @FragmentScope
    fun provideToothBrushModel(connection: KLTBConnection): ToothbrushModel =
        connection.toothbrush().model

    @Provides
    @Named(DI_GOAL_BRUSHING_TIME)
    @FragmentScope
    fun providesGoalBrushingTime(connector: IKolibreeConnector): Duration = Duration.ofSeconds(
        connector.currentProfile?.brushingGoalTime?.toLong()
            ?: DEFAULT_BRUSHING_GOAL.toLong()
    )

    @Provides
    @FragmentScope
    fun providesAppVersions(context: Context): KolibreeAppVersions =
        KolibreeAppVersions(context)
}

@Module
abstract class GameMiddlewareBindingModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [UnityGameMviActivityModule::class])
    internal abstract fun bindsGameMiddlewareMviActivity(): GameMiddlewareActivity
}

@Module
internal abstract class UnityGameFragmentModule {

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector
    internal abstract fun contributeUnityGameFragment(): UnityGameFragment
}

@Module(includes = [UnityGameFragmentModule::class])
internal class UnityGameMviActivityModule {

    @Provides
    @ToothbrushMac
    fun providesToothbrushMac(): Optional<String> = Optional.absent()
}

internal const val DI_GOAL_BRUSHING_TIME = "di_goal_brushing_time"
