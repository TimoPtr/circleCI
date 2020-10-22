package com.kolibree.sdkws.brushing.utils

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.sdkws.brushing.GAME1
import com.kolibree.sdkws.brushing.GAME2
import com.kolibree.sdkws.brushing.MockBrushingDao
import com.kolibree.sdkws.brushing.NEW_GAME
import com.kolibree.sdkws.brushing.PROFILE_ID_USER1
import com.kolibree.sdkws.brushing.PROFILE_ID_USER2
import com.kolibree.sdkws.brushing.createBrushingInternal
import com.kolibree.sdkws.brushing.currentTime
import com.kolibree.sdkws.brushing.persistence.dao.BrushingDao
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsDatastore
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsDatastoreImpl
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.room.ApiRoomDatabase
import java.util.Arrays
import org.mockito.Mock
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.temporal.ChronoUnit

abstract class BrushingHelperTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Mock
    private lateinit var brushingDao: BrushingDao

    private lateinit var apiRoomDatabase: ApiRoomDatabase

    internal val mockBrushingDao = MockBrushingDao()
    internal lateinit var brushingsDatastore: BrushingsDatastore

    protected fun startingDay(): OffsetDateTime {
        return currentTime().minusDays(10).truncatedTo(ChronoUnit.SECONDS)
    }

    protected fun initRoom() {
        apiRoomDatabase = providesAppDatabase(
            InstrumentationRegistry.getInstrumentation().targetContext
        )
        brushingDao = apiRoomDatabase.brushingDao()
        brushingsDatastore = BrushingsDatastoreImpl(brushingDao)
        createDummyData()
    }

    protected fun clearAll() {
        clearDB()
        mockBrushingDao.brushings.clear()
    }

    /**
     * Clean the content of the DB
     */
    protected fun clearDB() {
        apiRoomDatabase.clearAllTables()
    }

    protected fun addDeletedData() {

        val brushing1User1 = createBrushingInternal(
            profileId = PROFILE_ID_USER1, minusDay = 10,
            game = NEW_GAME, isDeletedLocally = true
        )
        val brushing2User1 = createBrushingInternal(
            profileId = PROFILE_ID_USER1, minusDay = 20,
            game = NEW_GAME, isDeletedLocally = true
        )
        brushingDao.addBrushing(brushing1User1)
        brushingDao.addBrushing(brushing2User1)
    }

    /**
     * Add some data in Room before the tests
     */
    private fun createDummyData() {

        val brushing1User1 = createBrushingInternal(PROFILE_ID_USER1, 1, GAME1)
        val brushing2User1 = createBrushingInternal(PROFILE_ID_USER1, 2, GAME2)
        val brushing3User1 = createBrushingInternal(PROFILE_ID_USER1, 3, GAME1)
        val brushing4User1 = createBrushingInternal(PROFILE_ID_USER1, 4, GAME2)
        val brushing5User1 = createBrushingInternal(PROFILE_ID_USER1, 5, GAME1)
        val brushing6User1 = createBrushingInternal(PROFILE_ID_USER1, 6, GAME2)
        val brushing7User1 = createBrushingInternal(PROFILE_ID_USER1, 7, GAME1)
        val brushing8User1 = createBrushingInternal(PROFILE_ID_USER1, 8, GAME2)
        val brushing1User2 = createBrushingInternal(PROFILE_ID_USER2, 1, GAME1)
        val brushing2User2 = createBrushingInternal(PROFILE_ID_USER2, 2, GAME2)

        brushing1User1.id = brushingDao.addBrushing(brushing1User1)
        brushing2User1.id = brushingDao.addBrushing(brushing2User1)
        brushing3User1.id = brushingDao.addBrushing(brushing3User1)
        brushing4User1.id = brushingDao.addBrushing(brushing4User1)
        brushing5User1.id = brushingDao.addBrushing(brushing5User1)
        brushing6User1.id = brushingDao.addBrushing(brushing6User1)
        brushing7User1.id = brushingDao.addBrushing(brushing7User1)
        brushing8User1.id = brushingDao.addBrushing(brushing8User1)
        brushing1User2.id = brushingDao.addBrushing(brushing1User2)
        brushing2User2.id = brushingDao.addBrushing(brushing2User2)

        mockBrushingDao.brushings.addAll(
            Arrays.asList(
                brushing1User1, brushing2User1, brushing3User1, brushing4User1, brushing5User1,
                brushing6User1, brushing7User1, brushing8User1, brushing1User2, brushing2User2
            )
        )
    }

    private fun providesAppDatabase(context: Context): ApiRoomDatabase {
        return Room.inMemoryDatabaseBuilder(context, ApiRoomDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    // extend Brushing by adding a compare method
    internal fun Brushing.compare(b: Brushing?): Boolean {
        return b?.let {
            this.game == it.game &&
                this.points == it.points &&
                this.duration == it.duration &&
                this.processedData == it.processedData &&
                this.dateTime == it.dateTime
        } == true
    }

    // extend BrushingInternal by adding a compare method
    internal fun BrushingInternal.compare(b: Brushing?): Boolean {
        return b?.let { it ->
            this.game == it.game &&
                this.points == it.points &&
                this.duration == it.duration
        } == true
    }
}
