package com.kolibree.android.sba.testbrushing

import com.google.common.base.Optional
import com.kolibree.android.app.mvi.brushstart.BrushStartConstants.Argument.PACKAGE_NAME
import com.kolibree.android.app.mvi.brushstart.BrushStartResourceProvider
import com.kolibree.android.game.ToothbrushMac
import com.kolibree.android.sba.testbrushing.ui.TestBrushingResourceProvider
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
internal object TestBrushingSessionModule {
    @Provides
    @Named(PACKAGE_NAME)
    fun providesPackageName(activity: TestBrushingActivity): String = activity.packageName

    @Provides
    @ToothbrushMac
    fun providesToothbrushMac(activity: TestBrushingActivity): Optional<String> =
        activity.extractMac()

    @Provides
    fun providesBrushStartResourceProvider(): BrushStartResourceProvider =
        TestBrushingResourceProvider()
}
