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
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.mocks.ProfileBuilder
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

internal class SelectProfileItemHandlerTest : BaseUnitTest() {

    private lateinit var handler: SelectProfileItemHandler

    private val profileFacade: ProfileFacade = mock()

    private val navigator: SelectProfileNavigator = mock()

    override fun setup() {
        super.setup()

        handler = SelectProfileItemHandler(profileFacade, navigator)
    }

    @Test
    fun `when new profile selected then app change active profile to selected one`() {
        val mainProfile = ProfileBuilder.create()
            .withId(123L)
            .build()
        val newProfileId = 404L
        val newProfileItem = profileItem(newProfileId, "New Profile")
        whenever(profileFacade.currentProfile)
            .thenReturn(mainProfile)
        whenever(profileFacade.setActiveProfileCompletable(newProfileId))
            .thenReturn(Completable.complete())

        handler.handle(newProfileItem).test().assertComplete()

        verify(profileFacade).setActiveProfileCompletable(newProfileId)
    }

    @Test
    fun `when the same profile selected then app does nothing`() {
        val mainProfileId = 200L
        val mainProfile = ProfileBuilder.create()
            .withId(mainProfileId)
            .build()
        val newProfileItem = profileItem(mainProfileId, "Main Profile")
        whenever(profileFacade.currentProfile)
            .thenReturn(mainProfile)

        handler.handle(newProfileItem).test().assertComplete()

        verify(profileFacade, never()).setActiveProfileCompletable(any())
    }

    @Test
    fun `when AddProfile selected then app navigates to AddProfile screen`() {
        val selectedItem = AddProfileItem()

        handler.handle(selectedItem).test().assertComplete()

        verify(navigator).showAddProfileScreen()
    }

    private fun profileItem(id: Long, name: String) = ProfileItem(
        profileId = id,
        profileName = name,
        profileAvatarUrl = null,
        creationDate = TrustedClock.getNowOffsetDateTime()
    )
}
