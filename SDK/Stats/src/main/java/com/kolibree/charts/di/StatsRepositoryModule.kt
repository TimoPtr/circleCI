package com.kolibree.charts.di

import com.kolibree.android.commons.interfaces.Truncable
import com.kolibree.charts.DashboardCalculatorView
import com.kolibree.charts.WeeklyStatCalculator
import com.kolibree.charts.persistence.repo.StatRepository
import com.kolibree.charts.persistence.repo.StatRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class StatsRepositoryModule {

    @Binds
    @IntoSet
    internal abstract fun bindsTruncableStatRepository(statRepository: StatRepositoryImpl): Truncable

    @Binds
    internal abstract fun bindsStatRepository(statRepository: StatRepositoryImpl): StatRepository

    @Binds
    internal abstract fun bindsDashboardCalculator(
        dashboardCalculator: WeeklyStatCalculator
    ): DashboardCalculatorView
}
