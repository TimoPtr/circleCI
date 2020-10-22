/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.tracker.logic.userproperties

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.extensions.getCountry
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.test.extensions.setFixedDate
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.tracker.logic.userproperties.UserProperties.ACCOUNT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.COUNTRY
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRMWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRST_DAY_OF_WEEK
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRST_NAME
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER_MALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS_RIGHT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HARDWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.LOCALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.MAC_ADDRESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PROFILE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PUB_ID
import com.kolibree.android.tracker.logic.userproperties.UserProperties.SERIAL_NUMBER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.STUDY_NAME
import com.kolibree.android.tracker.logic.userproperties.UserProperties.TOOTHBRUSH_MODEL
import com.kolibree.android.tracker.logic.userproperties.UserProperties.USER_TYPE
import com.kolibree.android.tracker.studies.StudiesForProfileUseCase
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.Locale
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.temporal.WeekFields

class UserPropertiesFactoryUnitTest : BaseUnitTest() {

    private val kolibreeConnector: InternalKolibreeConnector = mock()

    private val studiesForProfileUseCase: StudiesForProfileUseCase = mock()

    override fun setup() {
        super.setup()

        TrustedClock.setFixedDate()

        whenever(studiesForProfileUseCase.provide(any())).thenReturn(Single.just(EXPECTED_STUDIES))
    }

    @Test
    fun testEventDetailsKeys() {
        val info = createUserPropertiesFactory(getEmptyKolibreeConnector(), null)
        val eventDetails = info.toMap()

        assertEquals(16, eventDetails.size)

        assertNull(eventDetails[ACCOUNT])
        assertNull(eventDetails[PROFILE])
        assertNull(eventDetails[PUB_ID])
        assertNull(eventDetails[FIRST_NAME])
        assertEquals("U", eventDetails[GENDER])
        assertEquals(COUNTRY_VALUE, eventDetails[COUNTRY])
        assertEquals("U", eventDetails[HANDEDNESS])
        assertNull(eventDetails[USER_TYPE])

        assertNull(eventDetails[HARDWARE_VERSION])
        assertNull(eventDetails[FIRMWARE_VERSION])
        assertNull(eventDetails[MAC_ADDRESS])
        assertNull(eventDetails[SERIAL_NUMBER])
        assertNull(eventDetails[TOOTHBRUSH_MODEL])
        assertNull(eventDetails[STUDY_NAME])
    }

    @Test
    fun testNotConnectedToothbrush() {
        val info = createUserPropertiesFactory(getKolibreeConnector(), null)
        val eventDetails = info.toMap()

        assertEquals(16, eventDetails.size)

        assertEquals(ACCOUNT_ID.toString(), eventDetails[ACCOUNT])
        assertEquals(PROFILE_ID.toString(), eventDetails[PROFILE])
        assertEquals(PUB_ID_VALUE, eventDetails[PUB_ID])
        assertEquals(FIRST_NAME_VALUE, eventDetails[FIRST_NAME])
        assertEquals(GENDER_MALE, eventDetails[GENDER])
        assertEquals(COUNTRY_VALUE, eventDetails[COUNTRY])
        assertEquals(HANDEDNESS_RIGHT, eventDetails[HANDEDNESS])
        assertEquals("1", eventDetails[USER_TYPE])
        assertEquals(EXPECTED_STUDIES, eventDetails[STUDY_NAME])
        assertEquals(EXPECTED_LOCALE, eventDetails[LOCALE])
        assertEquals(EXPECTED_FIRST_DAY_OF_WEEK, eventDetails[FIRST_DAY_OF_WEEK])

        assertNull(eventDetails[HARDWARE_VERSION])
        assertNull(eventDetails[FIRMWARE_VERSION])
        assertNull(eventDetails[MAC_ADDRESS])
        assertNull(eventDetails[SERIAL_NUMBER])
        assertNull(eventDetails[TOOTHBRUSH_MODEL])
    }

    @Test
    fun testConnectedToothbrush() {
        val hwMajor = 34
        val hwMinor = 56
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val fwVersion = "2.1.0"
        val expectedModel = ToothbrushModel.CONNECT_E2

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC_VALUE)
            .withHardwareVersion(hwMajor, hwMinor)
            .withFirmwareVersion(fwVersion)
            .withSerialNumber(SERIAL_VALUE)
            .withModel(expectedModel)
            .build()

        val info = createUserPropertiesFactory(getKolibreeConnector(), connection)
        val eventDetails = info.toMap()

        assertEquals(16, eventDetails.size)
        assertEquals(ACCOUNT_ID.toString(), eventDetails[ACCOUNT])
        assertEquals(PROFILE_ID.toString(), eventDetails[PROFILE])
        assertEquals(PUB_ID_VALUE, eventDetails[PUB_ID])
        assertEquals(FIRST_NAME_VALUE, eventDetails[FIRST_NAME])
        assertEquals(GENDER_MALE, eventDetails[GENDER])
        assertEquals(COUNTRY_VALUE, eventDetails[COUNTRY])
        assertEquals(HANDEDNESS_RIGHT, eventDetails[HANDEDNESS])
        assertEquals("1", eventDetails[USER_TYPE])
        assertEquals(EXPECTED_LOCALE, eventDetails[LOCALE])
        assertEquals(EXPECTED_FIRST_DAY_OF_WEEK, eventDetails[FIRST_DAY_OF_WEEK])

        assertEquals(expectedHwVersion, eventDetails[HARDWARE_VERSION])
        assertEquals(fwVersion, eventDetails[FIRMWARE_VERSION])
        assertEquals(MAC_VALUE, eventDetails[MAC_ADDRESS])
        assertEquals(SERIAL_VALUE, eventDetails[SERIAL_NUMBER])
        assertEquals("E2", eventDetails[TOOTHBRUSH_MODEL])
    }

    private fun createDefaultProfile(): Profile = ProfileBuilder.create()
        .withName(FIRST_NAME_VALUE)
        .withMaleGender()
        .withHandednessRight()
        .withId(PROFILE_ID)
        .withCountry(COUNTRY_VALUE)
        .build()

    private fun createDefaultAccount(): AccountInternal = AccountInternal(
        ACCOUNT_ID,
        PROFILE_ID,
        PROFILE_ID
    ).also {
        it.pubId = PUB_ID_VALUE
        it.isBeta = true
    }

    private fun getKolibreeConnector(): IKolibreeConnector {
        val profile = createDefaultProfile()
        val account = createDefaultAccount()
        whenever(kolibreeConnector.currentAccount()).thenReturn(account)
        whenever(kolibreeConnector.currentProfile).thenReturn(profile)
        whenever(kolibreeConnector.accountId).thenReturn(ACCOUNT_ID)
        whenever(kolibreeConnector.pubId).thenReturn(PUB_ID_VALUE)
        return kolibreeConnector
    }

    private fun getEmptyKolibreeConnector(): IKolibreeConnector {
        whenever(kolibreeConnector.accountId).thenReturn(-1)
        whenever(kolibreeConnector.currentProfile).thenReturn(null)
        whenever(kolibreeConnector.pubId).thenReturn(null)
        whenever(kolibreeConnector.currentAccount()).thenReturn(null)
        return kolibreeConnector
    }

    private fun createUserPropertiesFactory(
        connector: IKolibreeConnector,
        connection: KLTBConnection?
    ): UserPropertiesFactory {
        val factory = UserPropertiesFactory(studiesForProfileUseCase)
        factory.fill(connector, connection)
        return factory
    }

    companion object {
        private const val ACCOUNT_ID = 13333L
        private const val PROFILE_ID = 123L
        private const val FIRST_NAME_VALUE = "FirstName"
        private const val PUB_ID_VALUE = "SAMPLE_PUB_ID"
        private val COUNTRY_VALUE = getCountry()
        private const val MAC_VALUE = "ca:fe:ba:be"
        private const val SERIAL_VALUE = "serial_001"
        private const val EXPECTED_STUDIES = "study1"
        private val EXPECTED_LOCALE = Locale.getDefault().displayName
        private val EXPECTED_FIRST_DAY_OF_WEEK = WeekFields
            .of(Locale.getDefault()).firstDayOfWeek.name
    }
}
