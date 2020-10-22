/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.pact.profile

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.PactTestExecutionContext
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.model.RequestResponsePact
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.profile.Gender
import com.kolibree.android.commons.profile.Handedness
import com.kolibree.android.testingpact.PactBaseTest
import com.kolibree.android.testingpact.state.PactProviderState
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.profile.ProfileApi
import io.pactfoundation.consumer.dsl.LambdaDsl
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull

class CreateProfilePactTest :
    PactBaseTest<PactProviderState.TestAccountExists>(PactProviderState.TestAccountExists) {

    private val testFirstName = "test first name"
    private val testGender = Gender.UNKNOWN
    private val testHandedness = Handedness.UNKNOWN

    override fun createPact(builder: PactDslWithProvider): RequestResponsePact {
        return builder
            .given(state.backendName)
            .uponReceiving("Create Profile")
            .path("/v4/accounts/${state.accountId}/profiles/")
            .method("POST")
            .willRespondWith()
            .status(200)
            .body(responseBody)
            .toPact()
    }

    override fun runTest(mockServer: MockServer, context: PactTestExecutionContext) {
        super.runTest(mockServer, context)

        val client = buildRetrofitClient(
            mockServer.getUrl(),
            ProfileApi::class.java
        )

        val createProfileData = generateCreateProfileData()

        val response = client.createProfile(state.accountId, createProfileData).blockingGet()

        assertEquals(200, response.code())
        assertNotNull(response.body())
        assertEquals(
            response.body(), defaultGson().fromJson(
                responseBody.toString(),
                ProfileInternal::class.java
            )
        )
    }

    private val responseBody = LambdaDsl.newJsonBody {
        it.stringValue(ProfileInternal.FIELD_FIRST_NAME, testFirstName)
            .stringValue(ProfileInternal.FIELD_GENDER, testGender.serializedName)
            .stringValue(ProfileInternal.FIELD_SURVEY_HANDEDNESS, testHandedness.serializedName)
    }.build()

    private fun generateCreateProfileData(): CreateProfileData {
        val createProfileData = CreateProfileData()

        createProfileData.firstName = testFirstName
        createProfileData.setGender(testGender)
        createProfileData.setHandedness(testHandedness)

        return createProfileData
    }
}
