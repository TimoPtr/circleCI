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

internal class DeleteAccountUseCaseTest : BaseUnitTest() {

    private lateinit var useCase: DeleteAccountUseCase

    private val accountFacade: AccountFacade = mock()

    private val googleSignInWrapper: GoogleSignInWrapper = mock()

    override fun setup() {
        super.setup()

        useCase = DeleteAccountUseCase(accountFacade, googleSignInWrapper)
    }

    @Test
    fun `deleteAccount invokes deletedeleteAccount on facade`() {
        whenever(accountFacade.deleteAccount()).thenReturn(Completable.complete())

        useCase.deleteAccount().test()

        verify(accountFacade).deleteAccount()
    }

    @Test
    fun `deleteAccount revokes access to google account on complete`() {
        whenever(accountFacade.deleteAccount()).thenReturn(Completable.complete())

        useCase.deleteAccount().test()

        verify(googleSignInWrapper).revokeAccess()
    }
}
