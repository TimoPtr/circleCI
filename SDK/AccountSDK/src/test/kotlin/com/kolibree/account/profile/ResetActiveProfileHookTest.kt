/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.account.profile

import com.kolibree.account.AccountFacade
import com.kolibree.account.toAccount
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.sdkws.core.IKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.CompletableSubject
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Test

class ResetActiveProfileHookTest : BaseUnitTest() {
    private val currentProfileProvider: CurrentProfileProvider = mock()
    private val accountFacade: AccountFacade = mock()
    private val connector: IKolibreeConnector = mock()
    private val timeoutScheduler = TestScheduler()

    private val hook = ResetActiveProfileHook(
        currentProfileProvider,
        accountFacade,
        connector,
        timeoutScheduler
    )

    @Test
    fun `onProfileDeleted returns connector setActiveProfileCompletable if deleted profileId was active`() {
        val deletedProfile = ProfileBuilder.create().build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(deletedProfile))

        val expectedActiveId = 6876L
        val account = createAccountInternal(ownerProfileId = expectedActiveId)
        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(account.toAccount()))

        val setActiveProfileSubject = CompletableSubject.create()
        whenever(connector.setActiveProfileCompletable(expectedActiveId))
            .thenReturn(setActiveProfileSubject)

        val observer = hook.onProfileDeleted(deletedProfile.id).test()
            .assertNotComplete()

        assertTrue(setActiveProfileSubject.hasObservers())
        setActiveProfileSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `onProfileDeleted never invokes connector setActiveProfileCompletable if deleted profileId wasn't active`() {
        val deletedProfile = ProfileBuilder.create().build()
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.just(deletedProfile))

        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(createAccountInternal().toAccount()))

        hook.onProfileDeleted(33 + deletedProfile.id).test()

        verify(connector, never()).setActiveProfileCompletable(any())
    }

    @Test
    fun `onProfileDeleted invokes connector setActiveProfileCompletable if currentProfileProvider doesn't return active profile within 1 second`() {
        val profileId = ProfileBuilder.create().build().id
        whenever(currentProfileProvider.currentProfileSingle())
            .thenReturn(Single.never())

        whenever(accountFacade.getAccountSingle())
            .thenReturn(Single.just(createAccountInternal().toAccount()))

        val observer = hook.onProfileDeleted(profileId).test()

        verify(connector, never()).setActiveProfileCompletable(any())
        observer.assertNotComplete()

        timeoutScheduler.advanceTimeBy(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        verify(connector).setActiveProfileCompletable(profileId)
        observer.assertComplete()
    }
}
