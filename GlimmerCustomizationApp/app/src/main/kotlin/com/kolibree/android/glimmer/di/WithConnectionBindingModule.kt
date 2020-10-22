package com.kolibree.android.glimmer.di

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.glimmer.tweaker.TweakerActivity
import com.kolibree.android.glimmer.tweaker.TweakerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class WithConnectionBindingModule {

    @ContributesAndroidInjector(modules = [TweakerModule::class])
    @ActivityScope
    internal abstract fun bindTweakerActivity(): TweakerActivity
}
