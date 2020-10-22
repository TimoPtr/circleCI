package com.kolibree.android.pirate.tuto

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.pirate.tuto.persistence.dao.TutorialDao
import com.kolibree.android.pirate.tuto.persistence.room.TutoRoomAppDatabase
import org.mockito.Mock

open class TutorialHelperTest {

    @Mock
    private lateinit var tutorialDao: TutorialDao

    private lateinit var roomDatabase: TutoRoomAppDatabase

    internal lateinit var tutoRepository: TutoRepository

    protected fun initRoom() {
        roomDatabase = providesAppDatabase(
                InstrumentationRegistry.getInstrumentation().targetContext
        )
        tutorialDao = roomDatabase.tutorialDao()
        tutoRepository = TutoRepositoryImpl(tutorialDao)
    }

    /**
     * Clean the content of the DB
     */
    protected fun clearDB() {
        roomDatabase.clearAllTables()
    }

    private fun providesAppDatabase(context: Context): TutoRoomAppDatabase =
        Room.inMemoryDatabaseBuilder(context, TutoRoomAppDatabase::class.java)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
}
