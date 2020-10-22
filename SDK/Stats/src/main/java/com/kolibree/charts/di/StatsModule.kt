package com.kolibree.charts.di

import com.kolibree.charts.inoff.di.InOffBrushingsCountModule
import com.kolibree.charts.persistence.room.StatsRoomModule
import com.kolibree.charts.synchronization.StatsSynchronizationModule
import dagger.Module

@Module(includes = [
    StatsRepositoryModule::class,
    StatsRoomModule::class,
    InOffBrushingsCountModule::class,
    StatsSynchronizationModule::class])
object StatsModule
