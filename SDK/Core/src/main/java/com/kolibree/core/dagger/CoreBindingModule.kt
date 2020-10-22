package com.kolibree.core.dagger

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.sba.testbrushing.TestBrushingActivity
import com.kolibree.android.sba.testbrushing.TestBrushingActivityModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CoreBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [TestBrushingActivityModule::class])
    internal abstract fun bindTestBrushingActivity(): TestBrushingActivity
}
