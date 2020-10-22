/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.api.request

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.data.model.RefreshTokenData
import junit.framework.TestCase.assertEquals
import org.junit.Test

class RefreshTokenRequestTest : BaseUnitTest() {

    @Test
    fun `produce proper request URL with correct query params`() {
        val accessToken = "THIS_IS_ME"
        val refreshToken = "PLEASE_REFRESH"
        val request = RefreshTokenRequest(
            RefreshTokenData(
                accessToken = accessToken, refreshToken = refreshToken
            )
        )
        assertEquals(
            "/v3/accounts/refresh_token/?access_token=$accessToken&refresh_token=$refreshToken",
            request.url
        )
    }
}
