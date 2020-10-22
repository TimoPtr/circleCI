/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings

import com.kolibree.account.AccountFacade
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.google.auth.GoogleSignInWrapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import org.junit.Test

internal class LogOutUseCaseTest : BaseUnitTest() {

    private lateinit var useCase: LogOutUseCase

    private val accountFacade: AccountFacade = mock()

    private val googleSignInWrapper: GoogleSignInWrapper = mock()

    override fun setup() {
        super.setup()

        useCase = LogOutUseCase(accountFacade, googleSignInWrapper)
    }

    @Test
    fun `logout invokes logout from accoutFacade`() {
        whenever(accountFacade.logout()).thenReturn(Completable.complete())

        useCase.logout().test()

        verify(accountFacade).logout()
    }

    @Test
    fun `logout unpairs google sign wrapper on complete`() {
        whenever(accountFacade.logout()).thenReturn(Completable.complete())

        useCase.logout().test()

        verify(googleSignInWrapper).unpairApp()
    }
}
