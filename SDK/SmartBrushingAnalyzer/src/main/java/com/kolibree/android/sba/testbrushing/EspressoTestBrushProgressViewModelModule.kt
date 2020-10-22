package com.kolibree.android.sba.testbrushing

import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sba.testbrushing.progress.TestBrushProgressViewModel
import dagger.Module
import dagger.Provides

/**
 * Do not used this class outside of Espresso test
 * There is an issue with kapt when this class is in the AndroidTest folder
 * https://stackoverflow.com/questions/54326219/dagger-android-not-generating-components-under-test-folder
 * TODO : check the SO link from time to time to check if there is any anwer
 */
@Module
class EspressoTestBrushProgressViewModelModule {

    @Provides
    internal fun providesTestBrushProgressViewModelFactory(
        navigator: TestBrushingNavigator,
        model: ToothbrushModel
    ): TestBrushProgressViewModel.Factory {
        return TestBrushProgressViewModel.Factory(navigator, EspressoBrushProgressController(), model)
    }
}
