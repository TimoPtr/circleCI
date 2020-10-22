package com.kolibree.android.tracker.logic

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.profile.HANDEDNESS_RIGHT
import com.kolibree.android.extensions.getCountry
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.ServiceProvisionResult
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.SameThreadExecutorService
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.logic.userproperties.UserProperties
import com.kolibree.android.tracker.logic.userproperties.UserProperties.ACCOUNT
import com.kolibree.android.tracker.logic.userproperties.UserProperties.BETA_USER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.COUNTRY
import com.kolibree.android.tracker.logic.userproperties.UserProperties.FIRMWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.GENDER_MALE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HANDEDNESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.HARDWARE_VERSION
import com.kolibree.android.tracker.logic.userproperties.UserProperties.MAC_ADDRESS
import com.kolibree.android.tracker.logic.userproperties.UserProperties.PROFILE
import com.kolibree.android.tracker.logic.userproperties.UserProperties.SERIAL_NUMBER
import com.kolibree.android.tracker.logic.userproperties.UserProperties.TOOTHBRUSH_MODEL
import com.kolibree.android.tracker.logic.userproperties.UserProperties.USER_TYPE
import com.kolibree.android.tracker.logic.userproperties.UserPropertiesFactory
import com.kolibree.android.tracker.studies.StudiesForProfileUseCase
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.data.model.Brushing
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Provider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.Mockito.verify

class KolibreeEventTrackerUnitTest : BaseUnitTest() {

    private val connector: InternalKolibreeConnector = mock()

    private val serviceProvider: ServiceProvider = mock()

    private val brushingsRepository: BrushingsRepository = mock()

    private val tracker: AnalyticsTracker = mock()

    private val studiesForProfileUseCase: StudiesForProfileUseCase = mock()

    override fun setup() {
        super.setup()

        val mockedProfile = createProfile()
        whenever(connector.pubId).thenReturn(PUB_ID)
        whenever(connector.beta).thenReturn(true)
        whenever(connector.currentProfile).thenReturn(mockedProfile)

        whenever(connector.accountId).thenReturn(ACCOUNT_ID.toLong())
        val mockedAccount: AccountInternal = mock()
        whenever(mockedAccount.id).thenReturn(ACCOUNT_ID.toLong())
        whenever(mockedAccount.isBeta).thenReturn(true)
        whenever(connector.currentAccount()).thenReturn(mockedAccount)

        whenever(studiesForProfileUseCase.provide(any()))
            .thenReturn(Single.just(""))
    }

    @Test
    fun sendEvent_invokesInternalTrackerSendEventMethod() {
        val eventTracker = createEventTracker()

        eventTracker.sendEvent(TEST_SCREEN_NAME)

        verify(tracker).sendEvent(eq(TEST_SCREEN_NAME.name), anyMap())
    }

    @Test
    fun sendEvent_invokesInternalTrackerSendEventMethodWithDetails() {
        val eventInfoMap = HashMap<String, String>()
        eventInfoMap[PROFILE] = PROFILE_ID.toString()
        eventInfoMap[ACCOUNT] = ACCOUNT_ID

        val userPropertiesFactory = mock<UserPropertiesFactory>()
        whenever(userPropertiesFactory.toMap()).thenReturn(eventInfoMap)

        val eventTracker = createEventTracker(Provider { userPropertiesFactory })

        eventTracker.sendEvent(TEST_SCREEN_NAME)

        verify(tracker).sendEvent(TEST_SCREEN_NAME.name, eventInfoMap)
    }

    @Test
    fun createEventInfo_containsToothbrushInformation() {
        val hwMajor = 34
        val hwMinor = 56
        val expectedHwVersion = "$hwMajor.$hwMinor"
        val fwMajor = 1
        val fwMinor = 14
        val fwRevision = 14432
        val expectedFwVersion = "$fwMajor.$fwMinor.$fwRevision"

        val connection = KLTBConnectionBuilder.createAndroidLess()
            .withMac(MAC)
            .withHardwareVersion(hwMajor, hwMinor)
            .withFirmwareVersion(fwMajor, fwMinor, fwRevision)
            .withSerialNumber(SERIAL)
            .withModel(ToothbrushModel.CONNECT_E1)
            .build()

        val service: KolibreeService = mock()
        whenever(service.knownConnections).thenReturn(listOf(connection))

        val eventTracker = createEventTracker()
        eventTracker.onKolibreeServiceConnected(service)

        val userProperties = eventTracker.createUserProperties()

        val eventDetails = userProperties.toMap()

        assertEquals(ACCOUNT_ID, eventDetails[ACCOUNT])
        assertEquals(PROFILE_ID.toString(), eventDetails[PROFILE])
        assertEquals(PUB_ID, eventDetails[UserProperties.PUB_ID])
        assertEquals(FIRST_NAME, eventDetails[UserProperties.FIRST_NAME])
        assertEquals(GENDER_MALE, eventDetails[GENDER])
        assertEquals(EXPECTED_COUNTRY, eventDetails[UserProperties.COUNTRY])
        assertEquals(HANDEDNESS_RIGHT, eventDetails[HANDEDNESS])
        assertEquals(BETA_USER, eventDetails[USER_TYPE])

        assertEquals(expectedFwVersion, eventDetails[FIRMWARE_VERSION])
        assertEquals(expectedHwVersion, eventDetails[HARDWARE_VERSION])
        assertEquals(MAC, eventDetails[MAC_ADDRESS])
        assertEquals(SERIAL, eventDetails[SERIAL_NUMBER])
        assertEquals("E1", eventDetails[TOOTHBRUSH_MODEL])
    }

    @Test
    fun createEventInfo_containsProfileInformation() {
        val eventTracker = createEventTracker()

        val userProperties = eventTracker.createUserProperties()

        val eventDetails = userProperties.toMap()

        assertEquals(ACCOUNT_ID, eventDetails[ACCOUNT])
        assertEquals(PROFILE_ID.toString(), eventDetails[PROFILE])
        assertEquals(PUB_ID, eventDetails[UserProperties.PUB_ID])
        assertEquals(FIRST_NAME, eventDetails[UserProperties.FIRST_NAME])
        assertEquals(GENDER_MALE, eventDetails[GENDER])
        assertEquals(EXPECTED_COUNTRY, eventDetails[COUNTRY])
        assertEquals(HANDEDNESS_RIGHT, eventDetails[HANDEDNESS])
        assertEquals(BETA_USER, eventDetails[USER_TYPE])
    }

    @Test
    fun createEventInfo_noKolibreeServiceNoTbInformation() {
        val eventTracker = createEventTracker()

        val userProperties = eventTracker.createUserProperties()

        val eventDetails = userProperties.toMap()

        assertEquals(ACCOUNT_ID, eventDetails[ACCOUNT])
        assertEquals(PROFILE_ID.toString(), eventDetails[PROFILE])
        assertEquals(PUB_ID, eventDetails[UserProperties.PUB_ID])
        assertEquals(FIRST_NAME, eventDetails[UserProperties.FIRST_NAME])
        assertEquals(GENDER_MALE, eventDetails[GENDER])
        assertEquals(EXPECTED_COUNTRY, eventDetails[UserProperties.COUNTRY])
        assertEquals(HANDEDNESS_RIGHT, eventDetails[HANDEDNESS])
        assertEquals(BETA_USER, eventDetails[USER_TYPE])

        assertNull(eventDetails[HARDWARE_VERSION])
        assertNull(eventDetails[FIRMWARE_VERSION])
        assertNull(eventDetails[SERIAL_NUMBER])
        assertNull(eventDetails[MAC_ADDRESS])
        assertNull(eventDetails[TOOTHBRUSH_MODEL])
    }

    @Test
    fun createEventInfo_noKolibreeServiceEmptyConnections() {
        val eventTracker = createEventTracker(Provider { mock<UserPropertiesFactory>() })

        val userProperties = eventTracker.createUserProperties()
        verify(userProperties).fill(any(), isNull())
    }

    /*
    GET LATEST USED CONNECTION
    */
    @Test
    fun getLatestUsedConnection_noService_returnsNull() {
        val eventTracker = createEventTracker()

        assertNull(eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_noConnections_returnsNull() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        whenever(service.knownConnections).thenReturn(emptyList())
        eventTracker.onKolibreeServiceConnected(service)

        assertNull(eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_oneConnection_returnsConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val expectedConnection = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(service.knownConnections).thenReturn(listOf(expectedConnection))
        eventTracker.onKolibreeServiceConnected(service)

        assertEquals(expectedConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_currentProfileIsNull_returnsFirstConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, connection2))
        eventTracker.onKolibreeServiceConnected(service)

        whenever(connector.currentProfile).thenReturn(null)

        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(null)

        assertEquals(firstConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_latestBrushingIsNull_returnsFirstConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, connection2))
        eventTracker.onKolibreeServiceConnected(service)

        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(null)

        assertEquals(firstConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_latestBrushingMacIsNull_returnsFirstConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, connection2))
        eventTracker.onKolibreeServiceConnected(service)

        val brushing = mock<Brushing>()
        whenever(brushing.toothbrushMac).thenReturn(null)
        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(brushing)

        assertEquals(firstConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_latestBrushingMacDoesNotMatchAny_returnsFirstConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, connection2))
        eventTracker.onKolibreeServiceConnected(service)

        val brushing = mock<Brushing>()
        whenever(brushing.toothbrushMac).thenReturn("random maccc")
        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(brushing)

        assertEquals(firstConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_latestBrushingMacMatchesFirstConnection_returnsFirstConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val connection2 = KLTBConnectionBuilder.createAndroidLess().withMac("second").build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, connection2))
        eventTracker.onKolibreeServiceConnected(service)

        val brushing = mock<Brushing>()
        whenever(brushing.toothbrushMac).thenReturn(KLTBConnectionBuilder.DEFAULT_MAC)
        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(brushing)

        assertEquals(firstConnection, eventTracker.latestUsedConnection)
    }

    @Test
    fun getLatestUsedConnection_withService_multipleConnections_latestBrushingMacMatchesSecondConnection_returnsSecondConnection() {
        val eventTracker = createEventTracker()

        val service = mock<KolibreeService>()
        val firstConnection = KLTBConnectionBuilder.createAndroidLess().build()
        val secondMac = "second"
        val secondConnection = KLTBConnectionBuilder.createAndroidLess().withMac(secondMac).build()
        whenever(service.knownConnections).thenReturn(listOf(firstConnection, secondConnection))
        eventTracker.onKolibreeServiceConnected(service)

        val brushing = mock<Brushing>()
        whenever(brushing.toothbrushMac).thenReturn(secondMac)
        whenever(brushingsRepository.getLastBrushingSession(PROFILE_ID)).thenReturn(brushing)

        assertEquals(secondConnection, eventTracker.latestUsedConnection)
    }

    /*
    onResume
    */

    @Test
    fun onResume_subscribesToConnectStream_storesServiceOnServiceConnected() {
        val subject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        val eventTracker = createEventTracker()
        eventTracker.onResume()

        assertTrue(subject.hasObservers())

        assertNull(eventTracker.weakService.get())

        val expectedService = mock<KolibreeService>()
        subject.onNext(ServiceConnected(expectedService))

        assertEquals(expectedService, eventTracker.weakService.get())
    }

    @Test
    fun onResume_subscribesToConnectStream_nullifiesServiceOnServiceDisconnected() {
        val subject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        val eventTracker = createEventTracker()
        eventTracker.onResume()

        assertTrue(subject.hasObservers())

        subject.onNext(ServiceConnected(mock()))

        assertNotNull(eventTracker.weakService.get())

        subject.onNext(ServiceDisconnected)

        assertNull(eventTracker.weakService.get())
    }

    @Test
    fun onResume_multipleInvocations_onlySubscribesToConnectStreamOnce() {
        val subject = PublishSubject.create<ServiceProvisionResult>()
        whenever(serviceProvider.connectStream()).thenReturn(subject)

        val eventTracker = createEventTracker()
        eventTracker.onResume()
        eventTracker.onResume()
        eventTracker.onResume()
        eventTracker.onResume()

        verify<ServiceProvider>(serviceProvider).connectStream()
    }

    /*
    Utils
    */

    private fun createEventTracker(
        eventInfoProvider: Provider<UserPropertiesFactory> =
            Provider { UserPropertiesFactory(studiesForProfileUseCase) }
    ): KolibreeEventTracker = KolibreeEventTracker(
        tracker,
        serviceProvider,
        connector,
        eventInfoProvider,
        brushingsRepository,
        SameThreadExecutorService.getInstance()
    )

    private fun createProfile(): Profile {
        return ProfileBuilder.create()
            .withName(FIRST_NAME)
            .withMaleGender()
            .withHandednessRight()
            .withId(PROFILE_ID)
            .withCountry(EXPECTED_COUNTRY)
            .build()
    }

    companion object {

        private const val PROFILE_ID: Long = 123
        private const val ACCOUNT_ID = "8008"
        private const val PUB_ID = "MYPUBID"
        private val EXPECTED_COUNTRY = getCountry()
        private val TEST_SCREEN_NAME = AnalyticsEvent("TestScreenName")
        private const val FIRST_NAME = "FirstName"
        private const val MAC = "ca:fe:ba:be"
        private const val SERIAL = "serial_001"
    }
}
