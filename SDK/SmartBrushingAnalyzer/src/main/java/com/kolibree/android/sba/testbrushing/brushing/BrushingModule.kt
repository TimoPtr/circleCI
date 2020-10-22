package com.kolibree.android.sba.testbrushing.brushing

import android.content.Context
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.game.bi.AvroCreatorModule
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.jaws.color.ColorJawsModule
import com.kolibree.android.mouthmap.logic.BrushingResults
import com.kolibree.android.mouthmap.logic.BrushingResultsMapper
import com.kolibree.android.mouthmap.logic.BrushingResultsMapperImpl
import com.kolibree.android.mouthmap.logic.ResultColorsProvider
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.crypto.AngleProvider
import com.kolibree.android.processedbrushings.crypto.ThresholdProvider
import com.kolibree.android.processedbrushings.crypto.TransitionProvider
import com.kolibree.android.processedbrushings.crypto.ZoneValidatorProvider
import com.kolibree.android.sba.testbrushing.TestBrushingActivity
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreator
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreatorKML
import com.kolibree.android.sba.testbrushing.brushing.creator.TestBrushingCreatorPlaqless
import com.kolibree.android.sdk.util.KpiSpeedProvider
import com.kolibree.android.sdk.util.KpiSpeedProviderModule
import com.kolibree.android.sdk.util.RnnWeightProvider
import com.kolibree.android.sdk.util.RnnWeightProviderModule
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(
    includes = [RnnWeightProviderModule::class,
        KpiSpeedProviderModule::class,
        ColorJawsModule::class,
        AvroCreatorModule::class
    ]
)
abstract class BrushingModule {

    @Binds
    @ActivityScope
    internal abstract fun bindsTestBrushingResultsProvider(
        implementation: TestBrushingResultsProviderImpl
    ): TestBrushingResultsProvider

    @Binds
    @ActivityScope
    internal abstract fun bindsTestBrushingDataMapper(
        implementation: BrushingResultsMapperImpl
    ): BrushingResultsMapper

    companion object {
        @Provides
        fun providesAppVersion(context: Context): KolibreeAppVersions = KolibreeAppVersions(context)

        @Provides
        internal fun providesBrushingResults(provider: TestBrushingResultsProvider): BrushingResults =
            provider.provide()

        @Provides
        fun providesModel(activity: TestBrushingActivity): ToothbrushModel = activity.extractModel()

        @Provides
        @ActivityScope
        fun providesResultColorsProvider(context: Context): ResultColorsProvider =
            ResultColorsProvider.create(context)
    }
}

@Module
class TestBrushingCreatorModule {

    @Provides
    @ActivityScope
    internal fun provideTestBrushingCreator(
        rnnWeightProvider: RnnWeightProvider?,
        kpiSpeedProvider: KpiSpeedProvider?,
        angleProvider: AngleProvider,
        transitionProvider: TransitionProvider,
        thresholdProvider: ThresholdProvider,
        zoneValidatorProvider: ZoneValidatorProvider,
        checkupCalculator: CheckupCalculator,
        connector: IKolibreeConnector,
        avroCreator: KmlAvroCreator,
        appVersions: KolibreeAppVersions,
        model: ToothbrushModel
    ): TestBrushingCreator {
        checkNotNull(rnnWeightProvider) { "WeightProvider should not be null did you provide the TB model" }
        checkNotNull(kpiSpeedProvider) { "KpiSpeedProvider should not be null did you provide the TB model" }

        return if (model == ToothbrushModel.PLAQLESS) {
            TestBrushingCreatorPlaqless(
                checkupCalculator,
                rnnWeightProvider,
                angleProvider,
                kpiSpeedProvider,
                transitionProvider,
                thresholdProvider,
                zoneValidatorProvider,
                model,
                connector,
                appVersions,
                avroCreator
            )
        } else {
            TestBrushingCreatorKML(
                checkupCalculator,
                rnnWeightProvider,
                angleProvider,
                kpiSpeedProvider,
                transitionProvider,
                thresholdProvider,
                zoneValidatorProvider,
                model,
                connector,
                appVersions,
                avroCreator
            )
        }
    }
}
