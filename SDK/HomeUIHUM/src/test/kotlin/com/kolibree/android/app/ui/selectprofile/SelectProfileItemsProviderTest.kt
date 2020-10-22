/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import com.kolibree.account.ProfileFacade
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZonedDateTime

internal class SelectProfileItemsProviderTest : BaseUnitTest() {

    private lateinit var provider: SelectProfileItemsProvider

    private val profileFacade: ProfileFacade = mock()

    override fun setup() {
        super.setup()

        provider = SelectProfileItemsProvider(profileFacade)
    }

    @Test
    fun `active profile is on first position`() {
        val activeProfile = createProfile(123L, "MainProfile")
        val otherProfiles = listOf(
            createProfile(201, "Xena"),
            createProfile(202, "MrBean"),
            createProfile(203, "Bob"),
            createProfile(204, "Zen")
        )
        mockProfiles(activeProfile, otherProfiles)

        val result = provider.selectProfileItems().blockingGet()
        assertTrue(result.size == 5)
        assertEquals("MainProfile", firstName(result.first()))
    }

    @Test
    fun `another profiles are in chronological order`() {
        val today = TrustedClock.getNowZonedDateTime()
        val activeProfile = createProfile(10L, "Center", today.minusDays(3))
        val otherProfiles = listOf(
            createProfile(1, "Dino", today.minusDays(4)),
            createProfile(2, "Celin", today.minusDays(2)),
            createProfile(3, "Bob", today.minusDays(1)),
            createProfile(4, "Alf", today.minusDays(5)),
            createProfile(5, "Efra", today.minusDays(6))
        )
        mockProfiles(activeProfile, otherProfiles)

        val result = provider.selectProfileItems().blockingGet()
        assertTrue(result.size == 6)
        assertEquals("Center", firstName(result[0]))
        assertEquals("Efra", firstName(result[1]))
        assertEquals("Alf", firstName(result[2]))
        assertEquals("Dino", firstName(result[3]))
        assertEquals("Celin", firstName(result[4]))
        assertEquals("Bob", firstName(result[5]))
    }

    @Test
    fun `AddProfile items is only visible if there is less then 4 profiles`() {
        mockProfiles(
            activeProfile = createProfile(1, "Main"),
            otherProfiles = listOf(
                createProfile(2, "Sub1"),
                createProfile(3, "Sub2")
            )
        )
        val threeProfilesResult = provider.selectProfileItems().blockingGet()
        assertTrue(threeProfilesResult.contains(AddProfileItem()))
        assertEquals(AddProfileItem(), threeProfilesResult.last())
        assertTrue(threeProfilesResult.size == 4)

        mockProfiles(
            activeProfile = createProfile(1, "Main"),
            otherProfiles = listOf(createProfile(2, "Second"))
        )
        val twoProfilesResult = provider.selectProfileItems().blockingGet()
        assertTrue(twoProfilesResult.contains(AddProfileItem()))
        assertEquals(AddProfileItem(), twoProfilesResult.last())
        assertTrue(twoProfilesResult.size == 3)
    }

    @Test
    fun `AddProfile items is not present if there is more then 3 profiles`() {
        mockProfiles(
            activeProfile = createProfile(1, "Main"),
            otherProfiles = listOf(
                createProfile(2, "Sub1"),
                createProfile(3, "Sub2"),
                createProfile(4, "Sub3")
            )
        )
        val threeProfilesResult = provider.selectProfileItems().blockingGet()
        assertFalse(threeProfilesResult.contains(AddProfileItem()))
        assertTrue(threeProfilesResult.size == 4)
    }

    private fun mockProfiles(activeProfile: Profile, otherProfiles: List<Profile>) {
        val allProfiles = (otherProfiles + activeProfile).shuffled()
        whenever(profileFacade.getProfilesList())
            .thenReturn(Single.just(allProfiles))
        whenever(profileFacade.currentProfile)
            .thenReturn(activeProfile)
    }

    private fun firstName(items: SelectProfileItem): String = when (items) {
        is ProfileItem -> items.profileName
        else -> ""
    }

    private fun createProfile(
        id: Long,
        name: String,
        creationDate: ZonedDateTime = TrustedClock.getNowZonedDateTime()
    ): Profile = ProfileBuilder.create()
        .withId(id)
        .withName(name)
        .withCreationDate(creationDate)
        .build()
}
