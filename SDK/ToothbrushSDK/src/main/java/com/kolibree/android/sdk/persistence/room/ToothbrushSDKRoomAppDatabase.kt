package com.kolibree.android.sdk.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomAppDatabase.Companion.VERSION
import com.kolibree.android.sdk.persistence.room.migrations.AddBootloaderColumnMigration
import com.kolibree.android.sdk.persistence.room.migrations.AddDirtyColumnMigration
import com.kolibree.android.sdk.persistence.room.migrations.AddDspColumnMigration

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(entities = [(AccountToothbrush::class)], version = VERSION)
@TypeConverters(AccountToothbrushConverters::class)
internal abstract class ToothbrushSDKRoomAppDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "kolibree-room-toothbrush-sdk.db"
        const val VERSION = 4

        val migrations = arrayOf(
            AddBootloaderColumnMigration,
            AddDirtyColumnMigration,
            AddDspColumnMigration
        )
    }

    abstract fun accountToothbrushDao(): AccountToothbrushDao
}
