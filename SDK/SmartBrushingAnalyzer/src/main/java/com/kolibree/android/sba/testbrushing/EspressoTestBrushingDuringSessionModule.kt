package com.kolibree.android.sba.testbrushing

import dagger.Module
import dagger.Provides

@Module
internal object EspressoTestBrushingDuringSessionModule {
    @Provides
    fun providesMac(activity: TestBrushingActivity): String {
        return activity.extractMac().get()
    }
}
