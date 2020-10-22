/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.app.ui.profile.NonActiveProfilesUseCase
import com.kolibree.android.app.ui.profile.NonActiveProfilesUseCaseImpl
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Test

class NonActiveProfilesUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: NonActiveProfilesUseCase

    private val connector: IKolibreeConnector = mock()

    private val profileProvider: CurrentProfileProvider = mock()

    override fun setup() {
        super.setup()

        useCase = NonActiveProfilesUseCaseImpl(connector, profileProvider)
    }

    @Test
    fun `profiles invokes connector profilesList method`() {
        val profile = ProfileBuilder.create().build()
        whenever(profileProvider.currentProfile()).thenReturn(profile)

        useCase.profiles()

        verify(connector).profileList
    }

    @Test
    fun `profiles filters current profile`() {
        val activeProfile = ProfileBuilder.create()
            .withId(1L)
            .build()

        val nonActiveProfile1 = ProfileBuilder.create()
            .withId(2L)
            .build()

        val nonActiveProfile2 = ProfileBuilder.create()
            .withId(3L)
            .build()

        val profiles = listOf(
            activeProfile,
            nonActiveProfile1,
            nonActiveProfile2
        )

        whenever(profileProvider.currentProfile()).thenReturn(activeProfile)
        whenever(connector.profileList).thenReturn(profiles)

        val result = useCase.profiles()

        assertEquals(2, result.size)
        assertEquals(nonActiveProfile1, result[0])
        assertEquals(nonActiveProfile2, result[1])
    }
}
