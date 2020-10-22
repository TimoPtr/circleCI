package com.kolibree.android.sba.testbrushing.progress

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sba.testbrushing.TestBrushingNavigator
import dagger.Module
import dagger.Provides

/**
 * Created by Guillaume Agis on 02/11/2018.
 */

@Module
internal object TestBrushingViewModelFactoryModule {

    @Provides
    internal fun providesTestBrushProgressViewModelFactory(
        navigator: TestBrushingNavigator,
        model: ToothbrushModel
    ): TestBrushProgressViewModel.Factory {
        return TestBrushProgressViewModel.Factory(
            navigator,
            TestBrushProgressControllerImpl(),
            model
        )
    }
}
