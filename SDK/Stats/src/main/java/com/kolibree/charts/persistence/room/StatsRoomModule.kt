package com.kolibree.charts.persistence.room

import android.content.Context
import androidx.room.Room
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.persistence.dao.StatDao
import dagger.Module
import dagger.Provides

@Module(includes = [StatsRoomDaoModule::class])
object StatsRoomModule {

    @Provides
    @AppScope
    @Suppress("SpreadOperator")
    fun providesStatDatabase(context: Context): StatsRoomAppDatabase = Room.databaseBuilder(
        context,
        StatsRoomAppDatabase::class.java,
        StatsRoomAppDatabase.DATABASE_NAME
    ).addMigrations(*StatsRoomAppDatabase.migrations).build()
}

@Module
object StatsRoomDaoModule {

    @Provides
    internal fun providesStatDao(appDatabase: StatsRoomAppDatabase): StatDao = appDatabase.statDao()

    @Provides
    internal fun providesInOffBrushingsCountDao(appDatabase: StatsRoomAppDatabase): InOffBrushingsCountDao =
        appDatabase.inOffBrushingsCountDao()
}
