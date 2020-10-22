package com.kolibree.android.pirate.tuto.persistence.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kolibree.android.pirate.tuto.persistence.dao.TutorialDao
import com.kolibree.android.pirate.tuto.persistence.model.Tutorial
import com.kolibree.android.pirate.tuto.persistence.room.TutoRoomAppDatabase.Companion.DATABASE_VERSION

/**
 * Created by guillaumeagis on 21/05/2018.
 * Creation of the Room Database
 */

@Database(entities = [(Tutorial::class)], version = DATABASE_VERSION)
internal abstract class TutoRoomAppDatabase : RoomDatabase() {

    internal companion object {
        const val DATABASE_NAME = "kolibree-room-tutorial.db"
        const val DATABASE_VERSION = 1
    }

    abstract fun tutorialDao(): TutorialDao
}
