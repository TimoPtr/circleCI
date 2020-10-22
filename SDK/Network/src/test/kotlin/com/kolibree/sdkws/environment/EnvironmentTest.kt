package com.kolibree.sdkws.environment

import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.android.network.environment.Environment
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Secutiry check to avoid commiting china versions pointing to our staging version
 */
class EnvironmentTest : CommonBaseTest() {

    @Test
    internal fun CHINA_pointsToExpectedServer() {
        assertEquals("https://connect.colgate.com.cn", Environment.CHINA.url())
    }

    @Test
    internal fun STAGING_pointsToExpectedServer() {
        assertEquals("https://api.s.kolibree.com", Environment.STAGING.url())
    }

    @Test
    internal fun DEV_pointsToExpectedServer() {
        assertEquals("https://api.d.kolibree.com", Environment.DEV.url())
    }

    @Test
    internal fun PRODUCTION_pointsToExpectedServer() {
        assertEquals("https://api.p.kolibree.com", Environment.PRODUCTION.url())
    }

    @Test(expected = IllegalAccessError::class)
    internal fun `reading CUSTOM url causes Exception`() {
        Environment.CUSTOM.url()
    }
}
