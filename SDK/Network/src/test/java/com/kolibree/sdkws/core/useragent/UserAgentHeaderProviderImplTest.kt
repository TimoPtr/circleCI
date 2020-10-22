package com.kolibree.sdkws.core.useragent

import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.network.core.useragent.USER_AGENT
import com.kolibree.android.network.core.useragent.UserAgentHeaderProviderImpl
import com.kolibree.android.network.retrofit.DeviceParameters
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock

/** Created by lookash on 30/12/2018. */
class UserAgentHeaderProviderImplTest : CommonBaseTest() {

    @Mock
    private lateinit var deviceParameters: DeviceParameters

    @Test
    fun userAgent() {
        // given
        whenever(deviceParameters.appVersion).thenReturn("1.2.3")
        whenever(deviceParameters.buildNumber).thenReturn(100)
        whenever(deviceParameters.deviceManufacturer).thenReturn("Google")
        whenever(deviceParameters.deviceModel).thenReturn("Pixel")
        whenever(deviceParameters.osVersion).thenReturn("9.0.1")
        whenever(deviceParameters.osApiLevel).thenReturn(26)

        // when
        val userAgentHeaderProvider =
            UserAgentHeaderProviderImpl(deviceParameters)
        val header = userAgentHeaderProvider.userAgent

        // then
        assertEquals(USER_AGENT, header.first)
        assertEquals("Dalvik/1.2.3 (Linux; U; 100; Google Pixel; Android 9.0.1; API 26)", header.second)
    }
}
