package com.kolibree.android.app.ui.settings.secret.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.android.sdk.persistence.room.AccountToothbrushConverters

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(entities = [(ModelAvailable::class)], version = 1)
@TypeConverters(AccountToothbrushConverters::class)
internal abstract class ModelsAvailableAppDatabase : RoomDatabase() {

    abstract fun modelsAvailableDao(): ModelsAvailableDao
}
