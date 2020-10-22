package com.kolibree.charts.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.kolibree.charts.persistence.dao.StatDao
import com.kolibree.charts.persistence.models.StatInternal
import com.kolibree.charts.persistence.room.StatsRoomAppDatabase.Companion.DATABASE_VERSION

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(
    entities = [StatInternal::class, InOffBrushingsCountEntity::class],
    version = DATABASE_VERSION
)
@TypeConverters(StatsConverters::class)
@VisibleForApp
abstract class StatsRoomAppDatabase : RoomDatabase() {

    @VisibleForApp
    companion object {
        // name use for the Room DB uses for the charts only
        const val DATABASE_NAME = "kolibree-room-stat.db"
        const val DATABASE_VERSION = 2
        val migrations = arrayOf(MigrationFrom1To2)
    }

    internal abstract fun statDao(): StatDao

    internal abstract fun inOffBrushingsCountDao(): InOffBrushingsCountDao
}
