/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.profile.models.IProfile
import com.kolibree.android.app.lifecycle.LifecycleDisposableScopeOwner
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.game.bi.KmlAvroCreator
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.android.processedbrushings.CheckupData
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.unity.BaseGameMiddlewareInstrumentationTest
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.game.middleware.CharVector
import com.kolibree.game.middleware.ProfileGender
import com.kolibree.game.middleware.ProfileHandedness
import com.kolibree.sdkws.brushing.wrapper.IBrushing.Companion.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.threeten.bp.Duration
import org.threeten.bp.temporal.ChronoUnit

@RunWith(AndroidJUnit4::class)
class WebServicesInteractorImplTest : BaseGameMiddlewareInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val lifecycle: Lifecycle = mock()
    private val lifecycleDisposableScopeOwner =
        LifecycleDisposableScopeOwner(lifecycle)
    private val profile: IProfile = mock()
    private val connector: IKolibreeConnector = mock()
    private val gameProgressRepository: GameProgressRepository = mock()
    private val appVersions = KolibreeAppVersions("1.0.0", "1")
    private val goalDuration = Duration.ofDays(1)
    private val checkupCalculator: CheckupCalculator = mock()
    private val avroCreator: KmlAvroCreator = mock()

    private val webServicesInteractor: WebServicesInteractorImpl by lazy {
        WebServicesInteractorImpl(
            lifecycleDisposableScopeOwner,
            profile,
            connector,
            gameProgressRepository,
            checkupCalculator,
            appVersions,
            goalDuration,
            avroCreator
        )
    }

    override fun setUp() {
        super.setUp()
        setupMocks()
    }

    @Test
    fun currentProfile_mapsCorrectlyFromDefaultValues() {
        val middlewareProfile: MiddlewareProfile = webServicesInteractor.currentProfile()

        assertEquals(ACCOUNT_ID.toInt(), middlewareProfile.accountId)
        assertEquals(PROFILE_ID.toInt(), middlewareProfile.profileId)
        assertEquals(PROFILE_FIRST_NAME, middlewareProfile.firstName)
        assertEquals(ProfileGender.MALE, middlewareProfile.gender)
        assertEquals(ProfileHandedness.RIGHT_HANDED, middlewareProfile.handedness)
        assertEquals(MINIMUM_BRUSHING_GOAL_TIME_SECONDS, middlewareProfile.brushingGoalTimeSeconds)
    }

    @Test
    fun currentProfile_mapsGenderCorrectly() {
        setupMocks(gender = Gender.MALE)
        assertEquals(ProfileGender.MALE, webServicesInteractor.currentProfile().gender)

        setupMocks(gender = Gender.FEMALE)
        assertEquals(ProfileGender.FEMALE, webServicesInteractor.currentProfile().gender)

        setupMocks(gender = Gender.UNKNOWN)
        assertEquals(ProfileGender.UNKNOWN, webServicesInteractor.currentProfile().gender)
    }

    @Test
    fun currentProfile_mapsHandednessCorrectly() {
        setupMocks(handedness = Handedness.RIGHT_HANDED)
        assertEquals(
            ProfileHandedness.RIGHT_HANDED,
            webServicesInteractor.currentProfile().handedness
        )

        setupMocks(handedness = Handedness.LEFT_HANDED)
        assertEquals(
            ProfileHandedness.LEFT_HANDED,
            webServicesInteractor.currentProfile().handedness
        )

        setupMocks(handedness = Handedness.UNKNOWN)
        assertEquals(
            ProfileHandedness.UNKNOWN,
            webServicesInteractor.currentProfile().handedness
        )
    }

    @Test
    fun getGameProgress_returnsProgressWhenDataIsValid() {
        assertEquals(
            DEFAULT_GAME_PROGRESS_JSON,
            webServicesInteractor.getGameProgress(DEFAULT_GAME_ID)
        )
        verify(gameProgressRepository).getProgress(PROFILE_ID, DEFAULT_GAME_ID)
    }

    @Test
    fun getGameProgress_returnsNullWhenRepoReturnsAnError() {
        doThrow(IllegalStateException("Ups, we don't have the progress"))
            .whenever(gameProgressRepository).getProgress(PROFILE_ID, DEFAULT_GAME_ID)

        assertNull(webServicesInteractor.getGameProgress(DEFAULT_GAME_ID))
    }

    @Test
    fun updateGameProgress_callsGameProgressRepositorySaveProgress() {
        val updatedGameProgressJson = "{ \"updated\": true }"
        setupMocks(gameProgressJson = updatedGameProgressJson)

        webServicesInteractor.updateGameProgress(DEFAULT_GAME_ID, updatedGameProgressJson)

        verify(gameProgressRepository).saveProgress(PROFILE_ID, DEFAULT_GAME_ID, updatedGameProgressJson)
    }

    @Test
    fun updateGameProgress_completesGracefully_ifGameProgressRepositorySaveProgressFails() {
        doReturn(Completable.error(IllegalStateException("Ups, save failed")))
            .whenever(gameProgressRepository).saveProgress(any(), any(), any())

        val updatedGameProgressJson = "{ \"updated\": true }"
        webServicesInteractor.updateGameProgress(DEFAULT_GAME_ID, updatedGameProgressJson)

        verify(gameProgressRepository).saveProgress(PROFILE_ID, DEFAULT_GAME_ID, updatedGameProgressJson)
    }

    @Test
    fun uploadBrushing_invokes_createBrushing_and_upload_avro() {
        val expectedProcessData = "{}"
        val expectedMac = "hello"
        val expectedSerial = "world"
        val expectedDuration = 1000L
        val expectedCoverage = 10
        val expectedAvroData = CharVector(listOf(1.toChar(), 2.toChar()))

        val profileWrapper = mock<ProfileWrapper>()
        val checkupData = mock<CheckupData>()

        TrustedClock.setFixedDate()

        val now = TrustedClock.getNowOffsetDateTime()

        whenever(checkupCalculator.calculateCheckup(any(), any(), any())).thenReturn(checkupData)
        whenever(checkupData.surfacePercentage).thenReturn(expectedCoverage)

        whenever(avroCreator.submitAvroData(any())).thenReturn(Completable.complete())

        whenever(connector.withProfileId(PROFILE_ID)).thenReturn(profileWrapper)

        doNothing().whenever(profileWrapper).createBrushing(any())

        webServicesInteractor.uploadBrushing(
            DEFAULT_GAME_ID,
            expectedProcessData,
            expectedMac,
            expectedSerial,
            now.toEpochSecond(),
            expectedDuration,
            expectedAvroData
        )

        val captorBrushingData = ArgumentCaptor.forClass(CreateBrushingData::class.java)
        verify(profileWrapper).createBrushing(captorBrushingData.capture())
        argumentCaptor<com.kolibree.kml.CharVector>().apply {
            verify(avroCreator).submitAvroData(capture())

            assertEquals(expectedAvroData, firstValue)
        }

        val capturedValue = captorBrushingData.value

        assertEquals(DEFAULT_GAME_ID, capturedValue.game)
        assertEquals(expectedProcessData, capturedValue.getProcessedData())
        assertEquals(expectedMac, capturedValue.mac)
        assertEquals(expectedSerial, capturedValue.serial)
        assertEquals(now.truncatedTo(ChronoUnit.SECONDS), capturedValue.date)
        assertEquals(Duration.ofMillis(expectedDuration), capturedValue.durationObject)
        assertEquals(expectedCoverage, capturedValue.coverage)
        assertEquals(goalDuration.seconds, capturedValue.goalDuration.toLong())
    }

    @Test
    fun webServicesInteractor_properlyHandlesLifecycleDisposableScopes() {
        testInteractorLifecycle(webServicesInteractor, lifecycleDisposableScopeOwner)
    }

    private fun setupMocks(
        accountId: Long = ACCOUNT_ID,
        profileId: Long = PROFILE_ID,
        profileFirstName: String = PROFILE_FIRST_NAME,
        brushingGoalTime: Int = DEFAULT_PROFILE_BRUSHING_GOAL_TIME,
        gender: Gender = DEFAULT_PROFILE_GENDER,
        handedness: Handedness = DEFAULT_PROFILE_HANDEDNESS,
        gameId: String = DEFAULT_GAME_ID,
        gameVersion: Int = DEFAULT_GAME_PROGRESS_VERSION,
        gameProgressJson: String = DEFAULT_GAME_PROGRESS_JSON
    ) {
        doReturn(profileId).whenever(profile).id
        doReturn(profileFirstName).whenever(profile).firstName
        doReturn(brushingGoalTime).whenever(profile).brushingGoalTime
        doReturn(gender).whenever(profile).gender
        doReturn(handedness).whenever(profile).handedness

        doReturn(ACCOUNT_ID).whenever(connector).accountId

        val gameProgress = GameProgress(gameId, gameProgressJson, TrustedClock.getNowZonedDateTimeUTC())

        doReturn(gameProgress)
            .whenever(gameProgressRepository).getProgress(profileId, gameId)
        doReturn(Completable.complete())
            .whenever(gameProgressRepository).saveProgress(profileId, gameId, gameProgressJson)
    }

    /*
    toMiddlewareType
     */

    @Test
    fun toMiddlewareType_mapsToTheCorrectProfileHandedness_forEachHandedness() {
        assertEquals(Handedness.RIGHT_HANDED.toMiddlewareType(), ProfileHandedness.RIGHT_HANDED)
        assertEquals(Handedness.LEFT_HANDED.toMiddlewareType(), ProfileHandedness.LEFT_HANDED)
        assertEquals(Handedness.UNKNOWN.toMiddlewareType(), ProfileHandedness.UNKNOWN)
    }

    @Test
    fun toMiddlewareType_mapsToTheCorrectProfileGender_forEachGender() {
        assertEquals(Gender.MALE.toMiddlewareType(), ProfileGender.MALE)
        assertEquals(Gender.FEMALE.toMiddlewareType(), ProfileGender.FEMALE)
        assertEquals(Gender.PREFER_NOT_TO_ANSWER.toMiddlewareType(), ProfileGender.UNKNOWN)
        assertEquals(Gender.UNKNOWN.toMiddlewareType(), ProfileGender.UNKNOWN)
    }

    private companion object {
        const val ACCOUNT_ID = 9983L
        const val PROFILE_ID = 12230L
        const val PROFILE_FIRST_NAME = "AwesomeProfile"
        const val DEFAULT_PROFILE_BRUSHING_GOAL_TIME = MINIMUM_BRUSHING_GOAL_TIME_SECONDS
        val DEFAULT_PROFILE_GENDER = Gender.MALE
        val DEFAULT_PROFILE_HANDEDNESS = Handedness.RIGHT_HANDED

        const val DEFAULT_GAME_ID = "SampleGameId"
        const val DEFAULT_GAME_PROGRESS_VERSION = 123
        const val DEFAULT_GAME_PROGRESS_JSON = "{}"
    }
}
