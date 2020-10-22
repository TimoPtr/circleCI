package com.kolibree.sdkws.room

import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.sdkws.brushing.persistence.dao.BrushingDao
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.gopirate.GoPirateDao
import com.kolibree.sdkws.data.model.gopirate.GoPirateData
import com.kolibree.sdkws.internal.OfflineUpdateDao
import com.kolibree.sdkws.internal.OfflineUpdateInternal
import com.kolibree.sdkws.room.ApiRoomDatabase.Companion.DATABASE_VERSION
import com.kolibree.sdkws.room.migrations.AddFakeBrushingMigration
import com.kolibree.sdkws.room.migrations.V2BrushingsMigration

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(
    entities = [BrushingInternal::class,
        OfflineUpdateInternal::class,
        GoPirateData::class], version = DATABASE_VERSION
)
internal abstract class ApiRoomDatabase : RoomDatabase() {

    @Keep
    companion object {
        const val DATABASE_NAME = "com.kolibree.sdkws.db"
        const val DATABASE_VERSION = 22

        val migrations = arrayOf(
            EmptyCompatMigration,
            MigrateBrushingInternal,
            V2BrushingsMigration,
            AddFakeBrushingMigration
        )
    }

    abstract fun brushingDao(): BrushingDao

    abstract fun offlineUpdateDao(): OfflineUpdateDao

    abstract fun goPirateDao(): GoPirateDao
}

//
// ALL MIGRATIONS FROM VERSION 1 TO VERSION 18 WERE INCORRECT
// Apparently this is because schema JSON files were modified
// after those migrations were created.
//
// Migration 19 has the same schema as migration 18.
//

private const val FIRST_DB_VERSION_ELIGIBLE_FOR_MIGRATIONS: Int = 19

private object EmptyCompatMigration : Migration(1, FIRST_DB_VERSION_ELIGIBLE_FOR_MIGRATIONS) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // no-op
    }
}
