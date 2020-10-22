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
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.BETA
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.COUNTRY
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.EMAIL
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.FIRST_NAME
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.GOOGLE_ID
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.GOOGLE_ID_TOKEN
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.PACKAGE_NAME
import com.kolibree.sdkws.account.models.GoogleRequestBodyTestConstraints.PARENTAL_CONSENT_GIVEN
import com.kolibree.sdkws.data.request.CreateAccountData
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GoogleSignUpRequestBodyTest : BaseUnitTest() {

    @Test
    fun `create body from package name and account creation data`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        val body = GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)

        assertEquals(
            GoogleSignUpRequestBody(
                packageName = PACKAGE_NAME,
                email = EMAIL,
                firstName = FIRST_NAME,
                country = COUNTRY,
                isBetaAccount = BETA,
                parentalConsentGiven = PARENTAL_CONSENT_GIVEN,
                googleId = GOOGLE_ID,
                googleIdToken = GOOGLE_ID_TOKEN
            ),
            body
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if email is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if Google ID is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if Google ID token is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if first name is null throw exception`() {
        val data = CreateAccountData.builder()
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if country is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setIsBetaAccount(BETA)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if beta flag is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setParentalConsentGiven(PARENTAL_CONSENT_GIVEN)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `if parental consent flag is null throw exception`() {
        val data = CreateAccountData.builder()
            .setFirstName(FIRST_NAME)
            .setEmail(EMAIL)
            .setCountry(COUNTRY)
            .setIsBetaAccount(BETA)
            .setGoogleId(GOOGLE_ID)
            .setGoogleIdToken(GOOGLE_ID_TOKEN)
            .build()
        GoogleSignUpRequestBody.createFrom(PACKAGE_NAME, data)
    }
}
