package com.kolibree.android.coachplus.settings.persistence.repo

import com.kolibree.android.commons.interfaces.Truncable
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
internal abstract class CoachSettingsRepoModule {

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableCoachSettingsRepository(
        coachSettingsRepository: CoachSettingsRepository
    ): Truncable

    @Binds
    internal abstract fun bindsCoachSettingsRepository(
        coachSettingsRepository: CoachSettingsRepositoryImpl
    ): CoachSettingsRepository
}
