/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.data.request

import com.google.gson.Gson
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CreateAccountDataTest {

    @Test
    fun `gender is preset with UNKNOWN`() {
        val accountData = CreateAccountData.builder().build()

        assertEquals(Gender.UNKNOWN.serializedName, accountData.gender)
    }

    /*
    setBirthday
     */

    @Test
    fun `builder setBirthday sets birthday in CreateAccountV3Data`() {
        val localDate = TrustedClock.getNowLocalDate()
        val result = CreateAccountData.builder().setBirthday(localDate).build()

        assertEquals(localDate, result.getBirthday())
    }

    /*
    setGender
     */

    @Test
    fun `builder's setGender uses Gender's serialized name`() {
        val expectedGender = Gender.MALE
        val accountData = CreateAccountData.builder().setGender(expectedGender).build()

        assertEquals(expectedGender.serializedName, accountData.gender)
    }

    /*
    setHandedness
     */

    @Test
    fun `builder setHandedness sets surveyHandedness in CreateAccountV3Data`() {
        val resultLeft = CreateAccountData.builder().setHandedness(Handedness.LEFT_HANDED).build()
        val resultRight = CreateAccountData.builder().setHandedness(Handedness.RIGHT_HANDED).build()
        val resultUnknown = CreateAccountData.builder().setHandedness(Handedness.UNKNOWN).build()

        assertEquals(Handedness.LEFT_HANDED, resultLeft.getSurveyHandedness())
        assertEquals(Handedness.RIGHT_HANDED, resultRight.getSurveyHandedness())
        assertEquals(Handedness.UNKNOWN, resultUnknown.getSurveyHandedness())
    }

    /*
    googleAvatarUrl
    */

    @Test
    fun `googleAvatarUrl doesn't appear in final JSON`() {
        val avatarUrl = "http://example.com/picture.jpg"
        val data = CreateAccountData.builder()
            .setGoogleId("1234")
            .setGoogleIdToken("AAABBBBFBFBFBFBFBFB")
            .setGoogleAvatarUrl(avatarUrl)
            .build()

        assertEquals(avatarUrl, data.googleAvatarUrl)

        val json = Gson().toJson(data)
        assertEquals(-1, json.indexOf(avatarUrl))
        assertEquals(-1, json.indexOf("googleAvatarUrl"))
    }

    /*
    builder
     */

    @Test
    fun `builder sets all classic field set all fields in CreateAccountV3Data`() {
        val result = CreateAccountData.builder()
            .setCountry("france")
            .setAppid("colgate connect")
            .setFirstName("kolibree")
            .setEmail("mail@kolibree.com")
            .setParentalEmail("parental@mail.com")
            .setPhoneNumber("007")
            .setVerificationToken("verification_token")
            .setVerificationCode("verification_code")
            .setIsBetaAccount(true)
            .setParentalConsentGiven(true)
            .setCommercialSubscription(true)
            .build()

        assertEquals("france", result.country)
        assertEquals("colgate connect", result.appid)
        assertEquals("kolibree", result.firstName)
        assertEquals("mail@kolibree.com", result.email)
        assertEquals("parental@mail.com", result.parentalEmail)
        assertEquals("007", result.phoneNumber)
        assertEquals("verification_token", result.verificationToken)
        assertEquals("verification_code", result.verificationCode)
        assertEquals(true, result.isBetaAccount)
        assertEquals(true, result.parentalConsentGiven)
        assertEquals(true, result.commercialSubscription)
    }

    @Test
    fun `builder sets all Google Sign-in related fields in  CreateAccountV3Data`() {
        val result = CreateAccountData.builder()
            .setAvatarUrl("http://my.jpg")
            .setGoogleId("123")
            .setGoogleIdToken("ABCDEFGH")
            .build()

        assertEquals("http://my.jpg", result.avatarUrl)
        assertEquals("123", result.googleId)
        assertEquals("ABCDEFGH", result.googleIdToken)
    }

    @Suppress("LongMethod")
    private fun createAccountDataWithGenderOnly(gender: String) =
        CreateAccountData(
            birthday = null,
            gender = gender,
            surveyHandedness = null,
            appid = null,
            commercialSubscription = false,
            country = null,
            email = null,
            firstName = null,
            isBetaAccount = false,
            parentalConsentGiven = false,
            parentalEmail = null,
            phoneNumber = null,
            verificationCode = null,
            verificationToken = null,
            avatarUrl = null,
            googleId = null,
            googleIdToken = null,
            googleAvatarUrl = null
        )
}
