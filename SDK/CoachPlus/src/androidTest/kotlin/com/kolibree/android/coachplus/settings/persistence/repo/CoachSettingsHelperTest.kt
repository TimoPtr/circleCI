package com.kolibree.android.coachplus.settings.persistence.repo

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.coachplus.settings.persistence.dao.CoachSettingsDao
import com.kolibree.android.coachplus.settings.persistence.room.CoachSettingsRoomAppDatabase
import com.kolibree.android.test.BaseInstrumentationTest
import org.mockito.Mock

open class CoachSettingsHelperTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Mock
    private lateinit var dao: CoachSettingsDao

    private lateinit var db: CoachSettingsRoomAppDatabase

    internal lateinit var repository: CoachSettingsRepository

    protected fun initRoom() {
        db = providesAppDatabase(context())
        dao = db.coachSettingsDao()
        repository = CoachSettingsRepositoryImpl(dao)
    }

    /**
     * Clean the content of the DB
     */
    protected fun clearDB() {
        db.clearAllTables()
    }

    private fun providesAppDatabase(context: Context): CoachSettingsRoomAppDatabase {
        return Room.inMemoryDatabaseBuilder(context, CoachSettingsRoomAppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}
