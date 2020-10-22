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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test

internal class SelectProfileDialogUseCaseImplTest : BaseUnitTest() {

    private lateinit var dialogUseCase: SelectProfileDialogUseCase

    private val selectProfileUseCase: SelectProfileUseCase = mock()

    private val navigator: SelectProfileNavigator = mock()

    override fun setup() {
        super.setup()

        dialogUseCase = SelectProfileDialogUseCaseImpl(selectProfileUseCase, navigator)
    }

    @Test
    fun `showDialogAndHandleSelectedItem prepares items for displaying`() {
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(emptyList()))

        dialogUseCase.showDialogAndHandleSelectedItem().test()

        verify(selectProfileUseCase).prepareItems()
    }

    @Test
    fun `showDialogAndHandleSelectedItem shows dialog`() {
        val profiles = listOf(
            ProfileItem(1L, "Profile", null, TrustedClock.getNowOffsetDateTime(), true),
            AddProfileItem()
        )
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(profiles))

        dialogUseCase.showDialogAndHandleSelectedItem().test()

        verify(navigator).selectProfileDialogMaybe(profiles)
    }

    @Test
    fun `showDialogAndHandleSelectedItem handles selected item if any`() {
        val selectedItem = ProfileItem(2L, "David", null, TrustedClock.getNowOffsetDateTime())
        val profiles = listOf(
            ProfileItem(1L, "Hawkins", null, TrustedClock.getNowOffsetDateTime(), true),
            selectedItem,
            AddProfileItem()
        )
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(profiles))
        whenever(navigator.selectProfileDialogMaybe(profiles))
            .thenReturn(Maybe.just(selectedItem))

        dialogUseCase.showDialogAndHandleSelectedItem().test()

        verify(selectProfileUseCase).handleSelectedItem(selectedItem)
    }

    @Test
    fun `showDialogAndHandleSelectedItem does nothing if new item was not selected`() {
        val profiles = listOf(
            ProfileItem(1L, "Active", null, TrustedClock.getNowOffsetDateTime(), true),
            AddProfileItem()
        )
        whenever(selectProfileUseCase.prepareItems())
            .thenReturn(Single.just(profiles))
        whenever(navigator.selectProfileDialogMaybe(profiles))
            .thenReturn(Maybe.empty())

        dialogUseCase.showDialogAndHandleSelectedItem().test()

        verify(selectProfileUseCase, never()).handleSelectedItem(any())
    }
}
