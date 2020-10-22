package com.android.synchronizator.network

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.models.SynchronizableKey.ACCOUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.BRUSHINGS
import com.kolibree.android.synchronizator.models.SynchronizableKey.CHALLENGE_CATALOG
import com.kolibree.android.synchronizator.models.SynchronizableKey.CHALLENGE_PROGRESS
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILES
import com.kolibree.android.synchronizator.network.Consumable
import com.kolibree.android.synchronizator.network.SynchronizeAccountApi
import com.kolibree.android.test.BaseMockWebServerTest
import com.kolibree.android.test.SharedTestUtils
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import junit.framework.TestCase.assertEquals
import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SynchronizeAccountResponseTest : BaseMockWebServerTest<SynchronizeAccountApi>() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    override fun retrofitServiceClass(): Class<SynchronizeAccountApi> {
        return SynchronizeAccountApi::class.java
    }

    override fun setUp() {
        super.setUp()

        FailEarly.overrideDelegateWith(NoopTestDelegate)
    }

    override fun tearDown() {
        super.tearDown()

        FailEarly.overrideDelegateWith(TestDelegate)
    }

    @Test
    fun parseResponse() {
        val jsonResponse =
            SharedTestUtils.getJson("account/synchronize/account_synchronize_response.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val synchronizeAccountResponse =
            retrofitService().synchronizationInfo(1, mapOf()).execute().body()!!

        assertEquals(4, synchronizeAccountResponse.typeConsumables.size)

        val expectedBrushingsConsumable = Consumable(
            version = 1278,
            updatedIds = listOf(1, 2, 3),
            deletedIds = listOf(4, 5)
        )

        val expectedProfilesConsumable = Consumable(
            version = 185,
            updatedIds = listOf(6, 7),
            deletedIds = listOf(8, 9)
        )

        val expectedChallengeProgressConsumable = Consumable(
            version = 20,
            updatedIds = listOf(10)
        )

        val expectedChallengesCatalogConsumable = Consumable(version = 5)

        assertEquals(
            expectedBrushingsConsumable,
            synchronizeAccountResponse.typeConsumables[BRUSHINGS]
        )

        assertEquals(
            expectedProfilesConsumable,
            synchronizeAccountResponse.typeConsumables[PROFILES]
        )

        assertEquals(
            expectedChallengeProgressConsumable,
            synchronizeAccountResponse.typeConsumables[CHALLENGE_PROGRESS]
        )

        assertEquals(
            expectedChallengesCatalogConsumable,
            synchronizeAccountResponse.typeConsumables[CHALLENGE_CATALOG]
        )
    }

    @Test
    fun parseResponse_withUnknownKey_parses_all_bundles() {
        val jsonResponse =
            SharedTestUtils.getJson("account/synchronize/account_synchronize_response2.json")

        val mockedResponse = MockResponse().setResponseCode(200).setBody(jsonResponse)

        mockWebServer.enqueue(mockedResponse)

        val synchronizeAccountResponse =
            retrofitService().synchronizationInfo(1, mapOf()).execute().body()!!

        assertEquals(3, synchronizeAccountResponse.typeConsumables.size)

        val expectedAccountConsumable = Consumable(
            version = 11,
            updatedIds = listOf(25538),
            deletedIds = listOf()
        )

        val expectedBrushingsConsumable = Consumable(
            version = 17,
            updatedIds = listOf(
                50305,
                50306,
                67427,
                67441,
                67445,
                67457,
                67460,
                67462,
                67465,
                67467,
                67468,
                67469,
                67471,
                67642,
                67648,
                67649,
                67662
            ),
            deletedIds = listOf()
        )

        val expectedProfilesConsumable = Consumable(
            version = 3,
            updatedIds = listOf(26616, 26622, 26633, 26660),
            deletedIds = listOf(26622, 26633, 26660)
        )

        assertEquals(expectedAccountConsumable, synchronizeAccountResponse.typeConsumables[ACCOUNT])

        assertEquals(
            expectedBrushingsConsumable,
            synchronizeAccountResponse.typeConsumables[BRUSHINGS]
        )

        assertEquals(
            expectedProfilesConsumable,
            synchronizeAccountResponse.typeConsumables[PROFILES]
        )
    }
}
