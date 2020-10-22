/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.appdata.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kolibree.sdkws.appdata.AppDataImpl

@Database(entities = [AppDataImpl::class], version = 1)
@TypeConverters(ZonedDateTimeConverter::class)
internal abstract class AppDataRoomDatabase : RoomDatabase() {

    abstract fun appDataDao(): AppDataDao
}
