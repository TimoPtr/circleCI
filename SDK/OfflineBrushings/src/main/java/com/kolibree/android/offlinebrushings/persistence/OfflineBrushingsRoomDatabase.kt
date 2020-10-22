package com.kolibree.android.offlinebrushings.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.room.ZoneOffsetConverter

/** Created by miguelaragues on 20/11/17.  */
@Database(entities = [OrphanBrushing::class], version = OfflineBrushingsRoomDatabase.VERSION)
@TypeConverters(ZoneOffsetConverter::class)
internal abstract class OfflineBrushingsRoomDatabase : RoomDatabase() {

    companion object {
        const val DATABASE_NAME = "kolibree-room-db"
        const val VERSION = 3

        val migrations = arrayOf(
            OfflineBrushingsRoomModule.migrationFrom1To2,
            OfflineBrushingMigrationFrom2To3
        )
    }

    abstract fun orphanBrushingDao(): OrphanBrushingDao
}
