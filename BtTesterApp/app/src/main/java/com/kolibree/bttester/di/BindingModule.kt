package com.kolibree.bttester.di

import com.kolibree.bttester.di.scopes.ActivityScope
import com.kolibree.bttester.freebrushing.FreeBrushingActivity
import com.kolibree.bttester.freebrushing.FreeBrushingModule
import com.kolibree.bttester.legacy.LegacyMainActivity
import com.kolibree.bttester.legacy.LegacyMainActivityModule
import com.kolibree.bttester.main.MainActivity
import com.kolibree.bttester.main.MainActivityModule
import com.kolibree.bttester.ota.di.OtaActivityModule
import com.kolibree.bttester.ota.mvi.OtaActivity
import com.kolibree.bttester.singleconnection.SingleConnectionActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/** Created by miguelaragues on 23/11/17.  */
@Module
internal abstract class BindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [MainActivityModule::class])
    abstract fun bindMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun bindSingleConnectionActivity(): SingleConnectionActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [LegacyMainActivityModule::class])
    abstract fun bindLegacyMainActivity(): LegacyMainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [OtaActivityModule::class])
    abstract fun bindOtaActivity(): OtaActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [FreeBrushingModule::class])
    abstract fun bindFreeBrushing(): FreeBrushingActivity
}