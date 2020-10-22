package com.kolibree.android.sdk.persistence.room

import android.content.Context
import androidx.room.Room
import com.kolibree.android.sdk.persistence.room.migrations.AddBootloaderColumnMigration
import com.kolibree.android.sdk.persistence.room.migrations.AddDirtyColumnMigration
import com.kolibree.android.sdk.persistence.room.migrations.AddDspColumnMigration
import dagger.Module
import dagger.Provides

@Module
object ToothbrushSDKRoomModule {
    private val lock = Any()

    @Volatile
    private var toothbrushSDKRoomAppDatabase: ToothbrushSDKRoomAppDatabase? = null

    @Provides
    internal fun providesToothbrushSDKRoomAppDatabase(context: Context): ToothbrushSDKRoomAppDatabase {
        if (toothbrushSDKRoomAppDatabase == null) {
            synchronized(lock) {
                if (toothbrushSDKRoomAppDatabase == null) {
                    toothbrushSDKRoomAppDatabase = Room.databaseBuilder(
                        context,
                        ToothbrushSDKRoomAppDatabase::class.java,
                        ToothbrushSDKRoomAppDatabase.DATABASE_NAME
                    )
                        .addMigrations(
                            AddBootloaderColumnMigration, AddDirtyColumnMigration, AddDspColumnMigration
                        )
                        .build()
                }
            }
        }
        return toothbrushSDKRoomAppDatabase!!
    }

    @Provides
    internal fun providesAccountToothbrushDao(
        appDatabase: ToothbrushSDKRoomAppDatabase
    ): AccountToothbrushDao = appDatabase.accountToothbrushDao()
}
