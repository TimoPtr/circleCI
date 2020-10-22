package com.kolibree.android.sba.testbrushing

import android.os.Handler
import com.kolibree.android.app.dagger.LostConnectionModule
import com.kolibree.android.app.dagger.scopes.FragmentScope
import com.kolibree.android.game.GameScope
import com.kolibree.android.mouthmap.MouthMapFragment
import com.kolibree.android.mouthmap.di.MouthMapTimerModule
import com.kolibree.android.sba.testbrushing.brushing.BrushingModule
import com.kolibree.android.sba.testbrushing.brushing.TestBrushingCreatorModule
import com.kolibree.android.sba.testbrushing.duringsession.TestBrushingDuringSessionFragment
import com.kolibree.android.sba.testbrushing.duringsession.timer.CarouselTimer
import com.kolibree.android.sba.testbrushing.duringsession.timer.CarouselTimerImpl
import com.kolibree.android.sba.testbrushing.intro.TestBrushIntroFragment
import com.kolibree.android.sba.testbrushing.optimize.OptimizeAnalysisFragment
import com.kolibree.android.sba.testbrushing.progress.TestBrushProgressFragment
import com.kolibree.android.sba.testbrushing.progress.TestBrushingViewModelFactoryModule
import com.kolibree.android.sba.testbrushing.results.ResultDescriptionProviderModule
import com.kolibree.android.sba.testbrushing.results.ResultsFragment
import com.kolibree.android.sba.testbrushing.results.ResultsModule
import com.kolibree.android.sba.testbrushing.results.plaqless.PlaqlessResultsFragment
import com.kolibree.android.sba.testbrushing.session.TestBrushingSessionFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Named

@Module(
    includes = [TestBrushingFragmentsModule::class,
        CarouselTimerModule::class,
        BrushingModule::class,
        TestBrushingCreatorModule::class,
        LostConnectionModule::class]
)
object TestBrushingActivityModule

@Module
abstract class TestBrushingFragmentsModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributeTestBrushIntroFragment(): TestBrushIntroFragment

    @FragmentScope
    @GameScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            TestBrushingSessionModule::class]
    )
    internal abstract fun contributeTestBrushingSessionFragment(): TestBrushingSessionFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            TestBrushingDuringSessionModule::class]
    )
    internal abstract fun contributeTestBrushingDuringSessionFragment(): TestBrushingDuringSessionFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributeOptimizeAnalysisFragment(): OptimizeAnalysisFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            ResultsModule::class,
            ResultDescriptionProviderModule::class]
    )
    internal abstract fun contributeResultsFragment(): ResultsFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = [TestBrushingNavigatorModule::class])
    internal abstract fun contributePlaqlessResultsFragment(): PlaqlessResultsFragment

    @FragmentScope
    @ContributesAndroidInjector(
        modules = [TestBrushingNavigatorModule::class,
            TestBrushingViewModelFactoryModule::class]
    )
    internal abstract fun contributeAnalysisProgressFragment(): TestBrushProgressFragment

    @FragmentScope
    @ContributesAndroidInjector
    internal abstract fun bindMouthMapFragment(): MouthMapFragment
}

@Module(includes = [MouthMapTimerModule::class])
internal abstract class CarouselTimerModule {

    @Binds
    abstract fun bindsCarouselTimer(implementation: CarouselTimerImpl): CarouselTimer

    companion object {

        const val CAROUSEL_TIMER = "CAROUSEL_TIMER"

        @Provides
        @Named(CAROUSEL_TIMER)
        fun providesCarouselHandler(): Handler {
            return Handler()
        }
    }
}
