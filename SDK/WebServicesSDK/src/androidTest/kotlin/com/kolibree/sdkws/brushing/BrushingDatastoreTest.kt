package com.kolibree.sdkws.brushing

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.reset
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.brushing.utils.BrushingHelperTest
import com.kolibree.sdkws.data.model.Brushing
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrushingDatastoreTest : BrushingHelperTest() {

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    @Before
    override fun setUp() {
        super.setUp()
        initRoom()
        TrustedClock.reset()
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearAll()
    }

    @Test
    fun verifyEmptyBrushing() {
        clearDB()
        assertEquals(
            0,
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_USER1).blockingGet().size
        )
        assertEquals(
            0,
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_USER2).blockingGet().size
        )
        assertEquals(
            0,
            brushingsDatastore.getBrushingsSince(
                startingDay(),
                PROFILE_ID_NO_DATA
            ).blockingGet().size
        )
    }

    @Test
    fun verifyReturnEmptyOrDefaultValueIfNoDataInDb() {
        clearDB()
        assertEquals(
            emptyList<Brushing>(),
            brushingsDatastore.getDeletedLocally().blockingGet()
        )
        assertEquals(
            emptyList<BrushingInternal>(),
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(null, brushingsDatastore.getLastBrushingSession(PROFILE_ID_USER1))
        assertEquals(
            emptyList<BrushingInternal>(),
            brushingsDatastore.getBrushingsByGame(GAME2)
        )
        assertEquals(
            emptyList<BrushingInternal>(),
            brushingsDatastore.getBrushingsBetween(
                currentTime().minusDays(4),
                currentTime(),
                PROFILE_ID_USER1
            ).blockingGet()
        )
        assertEquals(
            emptyList<BrushingInternal>(),
            brushingsDatastore.getBrushingsSince(currentTime(), PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            emptyList<BrushingInternal>(),
            brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER1).blockingGet()
        )
    }

    @Test
    fun deleteAll() {
        assertTrue(brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().isNotEmpty())
        brushingsDatastore.deleteAll().blockingAwait()
        assertTrue(brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().isEmpty())
    }

    @Test
    fun countDataForUserWithoutData() {
        assertEquals(
            mockBrushingDao.getBrushingsSince(startingDay(), PROFILE_ID_NO_DATA),
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_NO_DATA).blockingGet()
        )
    }

    @Test
    fun getDataForUsersWithData() {
        assertEquals(
            mockBrushingDao.getBrushingsSince(startingDay(), PROFILE_ID_USER1),
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingsSince(startingDay(), PROFILE_ID_USER2),
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun getDataForUsersWithDataFromADate() {

        val date = currentTime().minusDays(4).minusHours(1)

        assertEquals(
            mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER1),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER2),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun countDataForUsersWithDataGivenADateInTheFuture() {

        val date = currentTime().plusDays(4)

        assertEquals(
            mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER1),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER2),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun addBrushingToAprofileWithoutData() {

        val date = currentTime().minusDays(4)

        assertEquals(
            mockBrushingDao.getBrushingsSince(startingDay(), PROFILE_ID_NO_DATA),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_NO_DATA).blockingGet()
        )

        val brushingToAdd = BrushingInternal(
            profileId = PROFILE_ID_NO_DATA,
            duration = DURATION,
            datetime = currentTime(),
            game = GAME1,
            goalDuration = DURATION.toInt(),
            isSynchronized = true,
            idempotencyKey = UUID.randomUUID()
        )

        val id = brushingsDatastore.addBrushingIfDoNotExist(brushingToAdd)
        assertTrue(id > 0)
        assertEquals(
            1,
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_NO_DATA).blockingGet().size
        )
    }

    @Test
    fun addBrushingToAprofileWithData() {

        val date = currentTime().minusDays(4).minusHours(1)

        assertEquals(
            mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER1),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER1).blockingGet()
        )

        val brushingToAdd = BrushingInternal(
            profileId = PROFILE_ID_USER1,
            duration = DURATION,
            datetime = currentTime(),
            game = GAME1,
            goalDuration = DURATION.toInt(),
            isSynchronized = true,
            idempotencyKey = UUID.randomUUID()
        )

        val id = brushingsDatastore.addBrushingIfDoNotExist(brushingToAdd)
        assertTrue(id > 0)
        assertEquals(
            (
                mockBrushingDao.getBrushingsSince(date, PROFILE_ID_USER1).size + 1),
            brushingsDatastore.getBrushingsSince(date, PROFILE_ID_USER1).blockingGet().size
        )
    }

    @Test
    fun verifyCannotAddTwiceSameBrushing() {

        val brushingToAdd = BrushingInternal(
            profileId = PROFILE_ID_USER1,
            duration = DURATION,
            datetime = currentTime(),
            game = GAME1,
            goalDuration = DURATION.toInt(),
            isSynchronized = true,
            idempotencyKey = UUID.randomUUID()
        )

        val brushingToAdd2 = BrushingInternal(
            profileId = PROFILE_ID_USER1,
            duration = DURATION,
            datetime = currentTime(),
            game = GAME2,
            goalDuration = DURATION.toInt() + 10,
            isSynchronized = false,
            idempotencyKey = UUID.randomUUID()
        )

        val id = brushingsDatastore.addBrushingIfDoNotExist(brushingToAdd)
        assertTrue(id > 0)
        val id2 = brushingsDatastore.addBrushingIfDoNotExist(brushingToAdd2)
        assertTrue(id2 < 0)
    }

    @Test
    fun verifyGetBrushingEmitWithNoData() {
        clearDB()
        assertEquals(
            0,
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().size
        )
        assertTrue(brushingsDatastore.getBrushings(PROFILE_ID_USER2).blockingGet().isEmpty())
    }

    @Test
    fun verifyTotalBrushingForProfiles() {
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1),
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER2),
            brushingsDatastore.getBrushings(PROFILE_ID_USER2).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_NO_DATA),
            brushingsDatastore.getBrushingsSince(startingDay(), PROFILE_ID_NO_DATA).blockingGet()
        )
    }

    @Test
    fun verifyCountTotalBrushingForProfiles() {
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1).size.toLong(),
            brushingsDatastore.countBrushings(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER2).size.toLong(),
            brushingsDatastore.countBrushings(PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun verifyCountGameBrushingForProfiles() {
        assertEquals(
            mockBrushingDao.getBrushingsByGame(GAME1, PROFILE_ID_USER1).size.toLong(),
            brushingsDatastore.countBrushings(GAME1, PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingsByGame(GAME2, PROFILE_ID_USER2).size.toLong(),
            brushingsDatastore.countBrushings(GAME2, PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun getLastBrushingSession() {
        assertTrue(
            mockBrushingDao.getLastBrushingSession(PROFILE_ID_USER1).compare(
                brushingsDatastore.getLastBrushingSession(
                    PROFILE_ID_USER1
                )
            )
        )
        assertTrue(
            mockBrushingDao.getLastBrushingSession(PROFILE_ID_USER2).compare(
                brushingsDatastore.getLastBrushingSession(
                    PROFILE_ID_USER2
                )
            )
        )
        assertTrue(brushingsDatastore.getLastBrushingSession(PROFILE_ID_NO_DATA) == null)
    }

    @Test
    fun getBrushingsByGame() {
        assertEquals(
            mockBrushingDao.getBrushingsByGame(GAME1),
            brushingsDatastore.getBrushingsByGame(GAME1)
        )
        assertEquals(
            mockBrushingDao.getBrushingsByGame(GAME2),
            brushingsDatastore.getBrushingsByGame(GAME2)
        )
    }

    @Test
    fun getBrushingBetween() {
        val dateMinus4 = currentTime().minusDays(4).minusHours(1)
        val dateMinus3 = currentTime().minusDays(3).minusHours(1)

        assertEquals(
            mockBrushingDao.getBrushingBetween(dateMinus4, dateMinus3, PROFILE_ID_USER1),
            brushingsDatastore.getBrushingsBetween(
                dateMinus4,
                dateMinus3,
                PROFILE_ID_USER1
            ).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingBetween(dateMinus4, dateMinus3, PROFILE_ID_USER2),
            brushingsDatastore.getBrushingsBetween(
                dateMinus4,
                dateMinus3,
                PROFILE_ID_USER2
            ).blockingGet()
        )
    }

    @Test
    fun deleteLocally() {
        clearDB()
        val brushing = BrushingInternal(
            profileId = PROFILE_ID_USER1,
            duration = DURATION,
            datetime = currentTime(),
            game = GAME1,
            goalDuration = DURATION.toInt(),
            isSynchronized = true,
            idempotencyKey = UUID.randomUUID()
        )
        brushingsDatastore.addBrushingIfDoNotExist(brushing)

        assertEquals(1, brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().size)
        brushingsDatastore.deleteLocally(brushing.dateTime).blockingGet()
        assertEquals(0, brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().size)
    }

    @Test
    fun countGetBrushinSinceADay() {
        val dateMinus4 = currentTime().minusDays(4).minusHours(1)

        assertEquals(
            mockBrushingDao.getBrushingsSince(dateMinus4, PROFILE_ID_USER1).size.toLong(),
            brushingsDatastore.countBrushingsSince(dateMinus4, PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushingsSince(dateMinus4, PROFILE_ID_USER2).size.toLong(),
            brushingsDatastore.countBrushingsSince(dateMinus4, PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun getNonSynchronizedBrushing() {
        assertEquals(
            mockBrushingDao.getNonSynchronizedBrushing(PROFILE_ID_USER1),
            brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getNonSynchronizedBrushing(PROFILE_ID_USER2),
            brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_USER2).blockingGet()
        )

        assertEquals(
            mockBrushingDao.getNonSynchronizedBrushing(PROFILE_ID_NO_DATA),
            brushingsDatastore.getNonSynchronizedBrushing(PROFILE_ID_NO_DATA).blockingGet()
        )
    }

    @Test
    fun clearNonSynchronized() {
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1),
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER2),
            brushingsDatastore.getBrushings(PROFILE_ID_USER2).blockingGet()
        )
        mockBrushingDao.clearNonSynchronized(PROFILE_ID_USER1)
        brushingsDatastore.clearNonSynchronized(PROFILE_ID_USER1)
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1),
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet()
        )
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER2),
            brushingsDatastore.getBrushings(PROFILE_ID_USER2).blockingGet()
        )
    }

    @Test
    fun deleteBrushing() {
        val brushing = mockBrushingDao.getBrushings(PROFILE_ID_USER1).first()
        mockBrushingDao.deleteBrushing(brushing)
        brushingsDatastore.deleteBrushing(brushing).blockingAwait()
        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1).size,
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().size
        )

        assertEquals(
            mockBrushingDao.getBrushings(PROFILE_ID_USER1),
            brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet()
        )
    }

    @Test
    fun updateBrushingWithExistingBrushingStored() {
        val newPoint = 72
        val brushing = brushingsDatastore.getBrushings(PROFILE_ID_USER1).blockingGet().first()
        val newBrushing = brushing.copy(points = newPoint, isSynchronized = false)
        assertEquals(
            newPoint - POINTS,
            brushingsDatastore.updateBrushing(newBrushing)
        )
        brushingsDatastore.getBrushings(brushing.profileId).test()
            .await()
            .assertNoErrors()
            .assertValue { res ->
                res.contains(newBrushing)
            }
    }

    @Test
    fun updateBrushingWithoutExistingBrushingStored() {
        val newBrushing = createBrushingInternal(PROFILE_ID_NO_DATA, 10, GAME2)
        assertEquals(POINTS, brushingsDatastore.updateBrushing(newBrushing))
    }
}
