package com.kolibree.android.toothbrushupdate

import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.android.app.ui.ota.OtaUpdateActivity
import com.kolibree.android.app.ui.ota.OtaUpdateActivityModule
import com.kolibree.android.app.ui.ota.feature.AlwaysOfferOtaUpdateModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module(includes = [AlwaysOfferOtaUpdateModule::class])
abstract class ToothbrushUpdateModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(OtaUpdateActivityModule::class))
    internal abstract fun bindOtaUpdateActivity(): OtaUpdateActivity
}
