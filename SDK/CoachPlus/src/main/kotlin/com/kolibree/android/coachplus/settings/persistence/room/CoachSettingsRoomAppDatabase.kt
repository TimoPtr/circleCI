/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.settings.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.coachplus.settings.persistence.dao.CoachSettingsDao
import com.kolibree.android.coachplus.settings.persistence.model.CoachSettingsEntity

@Database(entities = [(CoachSettingsEntity::class)], version = 1)
internal abstract class CoachSettingsRoomAppDatabase : RoomDatabase() {

    abstract fun coachSettingsDao(): CoachSettingsDao
}
