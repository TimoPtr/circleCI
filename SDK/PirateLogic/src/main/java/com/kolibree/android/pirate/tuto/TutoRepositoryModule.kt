package com.kolibree.android.pirate.tuto

import com.kolibree.android.commons.interfaces.Truncable
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class TutoRepositoryModule {

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableTutoRepository(statRepository: TutoRepositoryImpl): Truncable

    @Binds
    internal abstract fun bindsTutoRepository(statRepository: TutoRepositoryImpl): TutoRepository
}
