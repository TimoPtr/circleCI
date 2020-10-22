/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.EMAIL
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.GOOGLE_ID
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.GOOGLE_ID_TOKEN
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.PACKAGE_NAME
import com.kolibree.sdkws.data.request.CreateAccountData
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GoogleLoginRequestBodyTest : BaseUnitTest() {

    @Test
    fun `create body from package name and login data`() {
        val data = CreateAccountData.builder()
            .setEmail(EMAIL)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        val body = GoogleLoginRequestBody.createFrom(PACKAGE_NAME, data)

        assertEquals(
            GoogleLoginRequestBody(
                packageName = PACKAGE_NAME,
                email = EMAIL,
                googleId = GOOGLE_ID,
                googleIdToken = GOOGLE_ID_TOKEN
            ),
            body
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if email is null throw exception`() {
        val data = CreateAccountData.builder()
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleLoginRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if Google ID is null throw exception`() {
        val data = CreateAccountData.builder()
            .setEmail(EMAIL)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleLoginRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if Google ID token is null throw exception`() {
        val data = CreateAccountData.builder()
            .setEmail(EMAIL)
            .setGoogleId(GOOGLE_ID)
            .build()
        GoogleLoginRequestBody.createFrom(PACKAGE_NAME, data)
    }
}
