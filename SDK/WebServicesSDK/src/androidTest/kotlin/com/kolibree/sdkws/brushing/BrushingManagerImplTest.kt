package com.kolibree.sdkws.brushing

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.base.Optional
import com.google.gson.JsonObject
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.sdkws.api.ConnectivityApiManagerImpl
import com.kolibree.sdkws.brushing.models.BrushingResponse
import com.kolibree.sdkws.brushing.utils.BrushingHelperTest
import com.kolibree.sdkws.brushing.utils.MockBrusingApi
import com.kolibree.sdkws.brushing.utils.MockBrusingApi.Companion.createBrushingResponse
import com.kolibree.sdkws.core.SynchronizationScheduler
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrushingManagerImplTest : BrushingHelperTest() {

    private val networkChecker = mock<NetworkChecker>()
    private val synchronizationScheduler = mock<SynchronizationScheduler>()
    private val checkupCalculator = mock<CheckupCalculator>()
    private val remoteBrushingsProcessor: RemoteBrushingsProcessor = mock()
    private val delegate = RetrofitHelperTest().generateDelegate<BrushingApi>()

    private lateinit var brushingApiManager: BrushingApiManager

    @get:Rule
    val thrown = ExpectedException.none()

    private val brushings: ArrayList<BrushingResponse> by lazy {
        arrayListOf(
            createBrushingResponse(PROFILE_ID_USER1, 8, GAME1, 10),
            createBrushingResponse(PROFILE_ID_USER1, 8, GAME2, 11),
            createBrushingResponse(PROFILE_ID_USER1, 7, GAME1, 12),
            createBrushingResponse(PROFILE_ID_USER1, 6, GAME2, 13),
            createBrushingResponse(PROFILE_ID_USER1, 5, GAME1, 14),
            createBrushingResponse(PROFILE_ID_USER1, 4, GAME1, 15),
            createBrushingResponse(PROFILE_ID_USER1, 3, GAME2, 16),
            createBrushingResponse(PROFILE_ID_USER1, 2, GAME1, 17),
            createBrushingResponse(PROFILE_ID_USER1, 1, GAME2, 18),
            createBrushingResponse(PROFILE_ID_USER1, 1, GAME1, 19),
            createBrushingResponse(PROFILE_ID_USER1, 1, GAME2, 20),
            createBrushingResponse(PROFILE_ID_USER1, 0, GAME1, 21),
            createBrushingResponse(PROFILE_ID_USER1, 0, GAME2, 22),

            createBrushingResponse(PROFILE_ID_USER2, 1, GAME1, 23),
            createBrushingResponse(PROFILE_ID_USER2, 0, GAME2, 24)
        )
    }

    @Before
    override fun setUp() {
        super.setUp()
        whenever(networkChecker.hasConnectivity()).thenReturn(true)
        val mockBrushingApi = MockBrusingApi(delegate, brushings)
        brushingApiManager = BrushingApiManagerImpl(
            mockBrushingApi,
            ConnectivityApiManagerImpl(synchronizationScheduler, networkChecker),
            checkupCalculator,
            Optional.of(remoteBrushingsProcessor)
        )
    }

    @Test
    fun getBrushingForAGivenUserWithoutConnectivity() {
        initWhenNoConnectivity()
        brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
    }

    @Test
    fun deleteBrushingForAGivenUserWithoutConnectivity() {
        initWhenNoConnectivity()
        brushingApiManager.deleteBrushings(ACCOUNT_ID, PROFILE_ID_USER1, emptyList()).blockingGet()
    }

    @Test
    fun assignBrushingForAGivenUserWithoutConnectivity() {
        initWhenNoConnectivity()
        brushingApiManager.assignBrushings(ACCOUNT_ID, PROFILE_ID_USER1, emptyList()).blockingGet()
    }

    @Test
    fun createBrushingForAGivenUserWithoutConnectivity() {
        initWhenNoConnectivity()
        brushingApiManager.createBrushing(
            ACCOUNT_ID,
            PROFILE_ID_USER1,
            createBrushingInternal(PROFILE_ID_USER1, 1, GAME1)
        ).blockingGet()
    }

    @Test
    fun createBrushingsForAGivenUserWithoutConnectivity() {
        initWhenNoConnectivity()
        brushingApiManager.createBrushings(ACCOUNT_ID, PROFILE_ID_USER1, emptyList()).blockingGet()
    }

    @Test
    fun deleteBrushingForAGivenUser() {
        val brushingsInit =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsInit.getBrushings().isNotEmpty())
        val brushingToDelete = brushingsInit.getBrushings().take(2).map { it.extractBrushing() }
        assertTrue(
            brushingApiManager.deleteBrushings(
                ACCOUNT_ID,
                PROFILE_ID_USER1,
                brushingToDelete
            ).blockingGet()
        )

        val brushingsListafterDeletion =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsListafterDeletion.getBrushings().isNotEmpty())
        assertEquals(
            brushingsInit.getBrushings().size - brushingToDelete.size,
            brushingsListafterDeletion.getBrushings().size
        )
    }

    @Test
    fun getBrushingForSeveralUsers() {
        val brushingsUser1 =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsUser1.getBrushings().isNotEmpty())

        val brushingsUser2 =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER2).blockingGet()
        assertTrue(brushingsUser2.getBrushings().isNotEmpty())

        val brushingsUserWithNoData =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_NO_DATA).blockingGet()
        assertTrue(brushingsUserWithNoData.getBrushings().isEmpty())
    }

    @Test
    fun createBrushingsListForAUser() {
        val brushingsBefore =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsBefore.getBrushings().isNotEmpty())

        val brushingToAdd = listOf(
            createBrushingInternal(PROFILE_ID_USER1, 10, GAME1, 12),
            createBrushingInternal(PROFILE_ID_USER1, 20, GAME2, 13)
        )
        val brushingsAfter =
            brushingApiManager.createBrushings(ACCOUNT_ID, PROFILE_ID_USER1, brushingToAdd)
                .blockingGet()

        assertTrue(brushingsAfter.getBrushings().isNotEmpty())
        assertEquals(
            brushingsBefore.getBrushings().size + brushingToAdd.size,
            brushingsAfter.getBrushings().size
        )
    }

    @Test
    fun createASingleBrushingForAUser() {
        val brushingsBefore =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsBefore.getBrushings().isNotEmpty())

        val brushingToAdd = createBrushingInternal(PROFILE_ID_USER1, 10, GAME1, 12)
        val brushingsAfter =
            brushingApiManager.createBrushing(ACCOUNT_ID, PROFILE_ID_USER1, brushingToAdd)
                .blockingGet()

        assertTrue(brushingsAfter.getBrushings().isNotEmpty())
        assertEquals(
            brushingsBefore.getBrushings().size + 1,
            brushingsAfter.getBrushings().size
        )
    }

    @Test
    fun assignBrushingForAUser() {
        val brushingsBefore =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        assertTrue(brushingsBefore.getBrushings().size > 2)
        val brushingsList = brushingsBefore.getBrushings().map { it.extractBrushing() }

        val firstBrushing = brushingsList[0]
        val secondBrushing = brushingsList[1]

        val brushingToAssign = listOf(
            Brushing(
                firstBrushing.duration, GOAL_DURATION, firstBrushing.dateTime,
                firstBrushing.coins, firstBrushing.points,
                PROCESSED_DATA.toString(), firstBrushing.profileId,
                firstBrushing.kolibreeId, firstBrushing.game
            ),
            Brushing(
                secondBrushing.duration, GOAL_DURATION2, secondBrushing.dateTime,
                secondBrushing.coins, secondBrushing.points,
                PROCESSED_DATA2.toString(), firstBrushing.profileId,
                secondBrushing.kolibreeId, secondBrushing.game
            )
        )

        assertTrue(
            brushingApiManager.assignBrushings(ACCOUNT_ID, PROFILE_ID_USER1, brushingToAssign)
                .blockingGet()
        )

        val brushingsAfter =
            brushingApiManager.getLatestBrushings(ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()
        val brushingsListAfter = brushingsAfter.getBrushings().map { it.extractBrushing() }
        assertTrue(brushingsListAfter.isNotEmpty())
        assertEquals(brushingsList.size, brushingsListAfter.size)

        val firstModifiedBrushing =
            brushingsListAfter.first { it.kolibreeId == firstBrushing.kolibreeId }
        val secondModifiedBrushing =
            brushingsListAfter.first { it.kolibreeId == secondBrushing.kolibreeId }

        assertEquals(GOAL_DURATION, firstModifiedBrushing.goalDuration)
        assertEquals(COINS, firstModifiedBrushing.coins)
        assertEquals(PROCESSED_DATA.toString(), firstModifiedBrushing.processedData)

        assertEquals(GOAL_DURATION2, secondModifiedBrushing.goalDuration)
        assertEquals(PROCESSED_DATA2.toString(), secondModifiedBrushing.processedData)
        assertEquals(COINS, secondModifiedBrushing.coins)

        // we no longer process points from backend
        assertEquals(0, firstModifiedBrushing.points)
        assertEquals(0, secondModifiedBrushing.points)
    }

    @Test
    fun fetchesBrushingsForProfileInGivenDateRange() {
        val endDate = TrustedClock.getNowLocalDate().minusDays(2)
        val startDate = endDate.minusDays(2)

        val brushings =
            brushingApiManager.getBrushingsInDateRange(
                ACCOUNT_ID,
                PROFILE_ID_USER1,
                fromDate = startDate,
                toDate = endDate,
                limit = 4
            ).blockingGet()

        assertEquals(3, brushings.getBrushings().size)
        val ids = brushings.getBrushings().map { it.kolibreeId }
        assertTrue(ids.containsAll(listOf(15L, 16L, 17L)))
    }

    @Test
    fun fetchesBrushingsForProfileBeforeChosenDate() {
        val startDate = TrustedClock.getNowLocalDate().minusDays(2)

        val brushings =
            brushingApiManager.getBrushingsInDateRange(
                ACCOUNT_ID,
                PROFILE_ID_USER1,
                fromDate = startDate,
                toDate = null
            ).blockingGet()

        assertEquals(6, brushings.getBrushings().size)
        val ids = brushings.getBrushings().map { it.kolibreeId }
        assertTrue(ids.containsAll(17L.rangeTo(22L).toList()))
    }

    @Test
    fun fetchesBrushingsForProfileAfterChosenDate() {
        val endDate = TrustedClock.getNowLocalDate().minusDays(2)

        val brushings =
            brushingApiManager.getBrushingsInDateRange(
                ACCOUNT_ID,
                PROFILE_ID_USER1,
                fromDate = null,
                toDate = endDate
            ).blockingGet()

        assertEquals(8, brushings.getBrushings().size)
        val ids = brushings.getBrushings().map { it.kolibreeId }
        assertTrue(ids.containsAll(10L.rangeTo(17L).toList()))
    }

    @Test
    fun fetchesBrushingsForProfileBeforeBrushingId() {
        val brushings =
            brushingApiManager.getBrushingsOlderThanBrushing(
                ACCOUNT_ID,
                PROFILE_ID_USER1,
                createBrushing(PROFILE_ID_USER1, kolibreeId = 16)
            ).blockingGet()

        assertEquals(6, brushings.getBrushings().size)
        val ids = brushings.getBrushings().map { it.kolibreeId }
        assertTrue(ids.containsAll(10L.rangeTo(15L).toList()))
    }

    private fun initWhenNoConnectivity() {
        whenever(networkChecker.hasConnectivity()).thenReturn(false)
        thrown.expect(RuntimeException::class.java)
        thrown.expectMessage("Network unavailable")
    }

    companion object {
        private const val GOAL_DURATION = 120
        private const val GOAL_DURATION2 = 150
        private const val QUALITY2 = 30
        private const val ACCOUNT_ID = 42L
        private val PROCESSED_DATA by lazy {
            val obj = JsonObject()
            obj.addProperty("prop", "processed_data")
            obj
        }
        private val PROCESSED_DATA2 by lazy {
            val obj = JsonObject()
            obj.addProperty("prop", "processed_data_n2")
            obj
        }
    }
}
