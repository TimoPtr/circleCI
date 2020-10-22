package com.kolibree.android.processedbrushings

import android.content.Context
import com.kolibree.android.feature.FeatureToggle
import com.kolibree.android.feature.impl.PersistentFeatureToggle
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.AngleProviderImpl
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProviderImpl
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProviderImpl
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProviderImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

@Module(includes = [ProcessedBrushingsBaseModule::class])
abstract class ProcessedBrushingsModule {

    @Binds
    internal abstract fun bindCheckupCalculator(
        checkupCalculator: CheckupCalculatorImpl
    ): CheckupCalculator
}

@Module(includes = [CheckupConfigurationModule::class])
abstract class ProcessedBrushingsBaseModule {

    @Binds
    internal abstract fun bindsAngleProvider(angleProviderImpl: AngleProviderImpl): AngleProvider

    @Binds
    internal abstract fun bindsTransitionProvider(transitionProviderImpl: TransitionProviderImpl): TransitionProvider

    @Binds
    internal abstract fun bindsThresholdProvider(thresholdProviderImpl: ThresholdProviderImpl): ThresholdProvider

    @Binds
    internal abstract fun bindsZoneValidator(zoneValidatorProviderImpl: ZoneValidatorProviderImpl):
        ZoneValidatorProvider
}

@Module
class CheckupConfigurationModule {

    @Provides
    @IntoSet
    fun providesCheckupGoalDurationConfigurationFeatureToggle(context: Context): FeatureToggle<*> =
        PersistentFeatureToggle(context, CheckupGoalDurationConfigurationFeature)
}
