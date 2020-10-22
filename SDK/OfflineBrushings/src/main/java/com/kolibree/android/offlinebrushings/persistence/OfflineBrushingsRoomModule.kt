package com.kolibree.android.offlinebrushings.persistence

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.offlinebrushings.persistence.OfflineBrushingsRoomDatabase.Companion.DATABASE_NAME
import dagger.Module
import dagger.Provides

@Module
internal object OfflineBrushingsRoomModule {

    /**
     * Drops RecordedSessionPersisted table
     */
    @VisibleForTesting
    val migrationFrom1To2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val request = "DROP TABLE recorded_session"
            database.execSQL(request)
        }
    }

    @Provides
    @AppScope
    internal fun providesAppDatabase(context: Context): OfflineBrushingsRoomDatabase {
        /*
        We use 'kolibree-room-db' as name to avoid having to migrate data from the app, which
        used this database to store orphan brushings before the modularization
         */
        return Room.databaseBuilder(
                context,
                OfflineBrushingsRoomDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(migrationFrom1To2)
            .addMigrations(OfflineBrushingMigrationFrom2To3)
            .build()
    }

    @Provides
    internal fun providesOrphanBrushingDao(appDatabase: OfflineBrushingsRoomDatabase): OrphanBrushingDao =
        appDatabase.orphanBrushingDao()
}
