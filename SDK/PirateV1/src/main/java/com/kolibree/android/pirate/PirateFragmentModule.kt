package com.kolibree.android.pirate

import dagger.Module
import dagger.Provides

/** Created by miguelaragues on 14/9/17.  */
@Module(includes = [BasePirateFragmentModule::class])
object PirateFragmentModule {

    @Provides
    internal fun providesBasePirateFragment(fragment: PirateFragment): BasePirateFragment {
        return fragment
    }
}
