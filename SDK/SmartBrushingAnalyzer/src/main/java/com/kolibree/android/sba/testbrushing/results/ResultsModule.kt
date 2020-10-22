package com.kolibree.android.sba.testbrushing.results

import com.kolibree.android.sba.testbrushing.TestBrushingActivity
import com.kolibree.android.sba.testbrushing.results.hint.ResultHintsPreferences
import com.kolibree.android.sba.testbrushing.results.hint.ResultHintsPreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
internal abstract class ResultsModule {

    @Binds
    abstract fun bindsResultHintPreferences(implementation: ResultHintsPreferencesImpl): ResultHintsPreferences
}

@Module
internal class ResultDescriptionProviderModule {

    @Provides
    fun provideMouthCoverageDescriptionProvider(activity: TestBrushingActivity) =
        MouthCoverageDescriptionProvider(activity)

    @Provides
    fun provideSpeedDescriptionProvider(activity: TestBrushingActivity) =
        SpeedDescriptionProvider(activity)
}
