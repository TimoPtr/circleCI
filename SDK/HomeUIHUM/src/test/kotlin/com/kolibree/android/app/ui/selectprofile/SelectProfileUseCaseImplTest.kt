/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectprofile

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Test

internal class SelectProfileUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: SelectProfileUseCaseImpl

    private val selectProfileItemsProvider: SelectProfileItemsProvider = mock()
    private val selectProfileItemHandler: SelectProfileItemHandler = mock()

    override fun setup() {
        super.setup()

        useCase = SelectProfileUseCaseImpl(
            selectProfileItemsProvider = selectProfileItemsProvider,
            selectProfileItemHandler = selectProfileItemHandler
        )
    }

    @Test
    fun `prepareItems returns items from provider`() {
        whenever(selectProfileItemsProvider.selectProfileItems())
            .thenReturn(Single.just(emptyList()))

        useCase.prepareItems().test().assertComplete()

        verify(selectProfileItemsProvider).selectProfileItems()
    }

    @Test
    fun `handleSelectedItem pass item to handler`() {
        val selectedItem = profileItem(12L, "MainProfile")
        whenever(selectProfileItemHandler.handle(selectedItem))
            .thenReturn(Completable.complete())

        useCase.handleSelectedItem(selectedItem).test().assertComplete()

        verify(selectProfileItemHandler).handle(selectedItem)
    }

    private fun profileItem(id: Long, name: String) = ProfileItem(
        profileId = id,
        profileName = name,
        profileAvatarUrl = null,
        creationDate = TrustedClock.getNowOffsetDateTime()
    )
}
