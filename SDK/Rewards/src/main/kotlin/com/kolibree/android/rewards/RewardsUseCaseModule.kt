package com.kolibree.android.rewards

import dagger.Binds
import dagger.Module

@Module
abstract class RewardsUseCaseModule {

    @Binds
    internal abstract fun bindsRewardsRepository(
        impl: CurrentProfileCategoriesWithProgressUseCaseImpl
    ): CurrentProfileCategoriesWithProgressUseCase
}
