/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.calendar.logic

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.calendar.logic.api.BrushingStreaksApi
import com.kolibree.android.calendar.logic.model.BrushingStreak
import com.kolibree.android.calendar.logic.persistence.BrushingStreaksDao
import com.kolibree.android.calendar.logic.persistence.CalendarRoomDatabase
import com.kolibree.android.calendar.logic.persistence.model.BrushingStreakEntity
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate

@RunWith(AndroidJUnit4::class)
internal class CalendarBrushingsRepositoryIntegrationTest : BaseMockWebServerTest<BrushingStreaksApi>() {

    private val profileId = 1234L

    override fun retrofitServiceClass() = BrushingStreaksApi::class.java

    private lateinit var database: CalendarRoomDatabase

    private lateinit var brushingStreaksDao: BrushingStreaksDao

    private lateinit var repository: CalendarBrushingsRepository

    private var brushingsRepository: BrushingsRepository = mock()

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun setUp() {
        super.setUp()

        initRoom()

        repository = CalendarBrushingsRepository(
            retrofitService(),
            brushingStreaksDao,
            AlwaysOnNetworkChecker,
            brushingsRepository,
            false
        )
    }

    override fun tearDown() {
        super.tearDown()
        database.close()
    }

    private fun initRoom() {
        database = Room.inMemoryDatabaseBuilder(context(), CalendarRoomDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        brushingStreaksDao = database.brushingStreaksDao()
    }

    @Test
    fun getStreaksForProfile_fetchesAndSavesStreaksFromApi() {
        mockResponse(responseCode = 200, responseBodyResourcePath = "calendar/brushing_streaks_set1.json")

        val observer = repository.getStreaksForProfile(profileId).test()

        observer.assertNoErrors()
        observer.assertValueCount(2)
        observer.assertValueAt(0, emptySet())
        observer.assertValueAt(
            1, setOf(
                BrushingStreak(LocalDate.of(2019, 1, 20), LocalDate.of(2019, 1, 26)),
                BrushingStreak(LocalDate.of(2019, 1, 27), LocalDate.of(2019, 2, 2)),
                BrushingStreak(LocalDate.of(2019, 2, 3), LocalDate.of(2019, 2, 9))
            )
        )

        assertEquals(
            listOf(
                BrushingStreakEntity(1, profileId, LocalDate.of(2019, 1, 20), LocalDate.of(2019, 1, 26)),
                BrushingStreakEntity(2, profileId, LocalDate.of(2019, 1, 27), LocalDate.of(2019, 2, 2)),
                BrushingStreakEntity(3, profileId, LocalDate.of(2019, 2, 3), LocalDate.of(2019, 2, 9))
            ),
            brushingStreaksDao.queryByProfile(profileId)
        )
    }

    @Test
    fun getStreaksForProfile_replacesStreaksInTheDatabase() {
        mockResponse(responseCode = 200, responseBodyResourcePath = "calendar/brushing_streaks_set1.json")

        repository.getStreaksForProfile(profileId).test()

        mockResponse(responseCode = 200, responseBodyResourcePath = "calendar/brushing_streaks_set2.json")

        val observer = repository.getStreaksForProfile(profileId).test()

        observer.assertNoErrors()
        observer.assertValueCount(2)
        observer.assertValueAt(
            0, setOf(
                BrushingStreak(LocalDate.of(2019, 1, 20), LocalDate.of(2019, 1, 26)),
                BrushingStreak(LocalDate.of(2019, 1, 27), LocalDate.of(2019, 2, 2)),
                BrushingStreak(LocalDate.of(2019, 2, 3), LocalDate.of(2019, 2, 9))
            )
        )
        observer.assertValueAt(
            1, setOf(
                BrushingStreak(LocalDate.of(2019, 1, 23), LocalDate.of(2019, 1, 28)),
                BrushingStreak(LocalDate.of(2019, 1, 29), LocalDate.of(2019, 2, 9)),
                BrushingStreak(LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15))
            )
        )

        assertEquals(
            listOf(
                BrushingStreakEntity(4, profileId, LocalDate.of(2019, 1, 23), LocalDate.of(2019, 1, 28)),
                BrushingStreakEntity(5, profileId, LocalDate.of(2019, 1, 29), LocalDate.of(2019, 2, 9)),
                BrushingStreakEntity(6, profileId, LocalDate.of(2019, 2, 11), LocalDate.of(2019, 2, 15))
            ),
            brushingStreaksDao.queryByProfile(profileId)
        )
    }

    private fun mockResponse(responseCode: Int, responseBodyResourcePath: String) {
        val jsonResponse = SharedTestUtils.getJson(responseBodyResourcePath)

        val mockedResponse = MockResponse().setResponseCode(responseCode).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)
    }

    private object AlwaysOnNetworkChecker : NetworkChecker {

        override fun hasConnectivity() = true

        override fun connectivityStateObservable() = Observable.just(true)
    }
}
