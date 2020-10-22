package com.kolibree.android.sba.testbrushing

import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.game.GameScope
import com.kolibree.android.mouthmap.MouthMapFragment
import com.kolibree.android.sba.testbrushing.duringsession.TestBrushingDuringSessionFragment
import com.kolibree.android.sba.testbrushing.intro.TestBrushIntroFragment
import com.kolibree.android.sba.testbrushing.optimize.OptimizeAnalysisFragment
import com.kolibree.android.sba.testbrushing.progress.TestBrushProgressFragment
import com.kolibree.android.sba.testbrushing.results.ResultDescriptionProviderModule
import com.kolibree.android.sba.testbrushing.results.ResultsFragment
import com.kolibree.android.sba.testbrushing.results.plaqless.PlaqlessResultsFragment
import com.kolibree.android.sba.testbrushing.session.TestBrushingSessionFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * Do not used this class outside of Espresso test
 * There is an issue with kapt when this TestBrushProgressViewModelTest.ktclass is in the AndroidTest folder
 * https://stackoverflow.com/questions/54326219/dagger-android-not-generating-components-under-test-folder
 * TODO : check the SO link from time to time to check if there is any anwer
 */
@Module
abstract class EspressoTestBrushingFragmentsModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributeTestBrushIntroFragment(): TestBrushIntroFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class, TestBrushingSessionModule::class])
    internal abstract fun contributeTestBrushingSessionFragment(): TestBrushingSessionFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            EspressoTestBrushingDuringSessionModule::class]
    )
    internal abstract fun contributeTestBrushingDuringSessionFragment(): TestBrushingDuringSessionFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributeOptimizeAnalysisFragment(): OptimizeAnalysisFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            ResultDescriptionProviderModule::class]
    )
    internal abstract fun contributeAnalysisResultFragment(): ResultsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributePlaqlessResultsFragment(): PlaqlessResultsFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            EspressoTestBrushProgressViewModelModule::class]
    )
    internal abstract fun contributeAnalysisProgressFragment(): TestBrushProgressFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun bindMouthMapFragment(): MouthMapFragment
}
