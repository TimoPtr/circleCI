package com.kolibree.charts

import android.content.Context
import androidx.room.Room
import com.kolibree.charts.persistence.room.StatsRoomAppDatabase

/**
 * Created by guillaumeagis on 22/05/2018.
 * Create a Room Database in memory
 */

internal class MockDatabaseProvider {

    internal fun providesAppDatabase(context: Context): StatsRoomAppDatabase {
        return Room.inMemoryDatabaseBuilder(context, StatsRoomAppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}
