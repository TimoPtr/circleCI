package com.kolibree.android.sba.testbrushing

import dagger.Binds
import dagger.Module

@Module
internal abstract class TestBrushingNavigatorModule {
    @Binds
    abstract fun bindsTestBrushingNavigator(activity: TestBrushingActivity): TestBrushingNavigator
}
