/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.toothbrush

import com.kolibree.account.utils.ForgottenToothbrush
import com.kolibree.account.utils.ToothbrushForgetterImpl
import com.kolibree.account.utils.ToothbrushForgottenHook
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.KolibreeService
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.android.sdk.core.UnknownToothbrushException
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.repo.ToothbrushRepository
import com.kolibree.android.test.TestForcedException
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountToothbrush
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.subjects.CompletableSubject
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class ToothbrushForgetterImplTest : BaseUnitTest() {

    private val serviceProvider: ServiceProvider = mock()
    private val toothbrushRepository: ToothbrushRepository = mock()

    private lateinit var toothbrushForgetter: ToothbrushForgetterImpl

    /*
    forgetToothbrush
     */

    @Test
    fun `forgetToothbrush subscribes to service forgetCompletable with toothbrush mac`() {
        initWithHooks()

        val mac = "mac"
        whenever(toothbrushRepository.getAccountToothbrush(mac))
            .thenReturn(Maybe.just(createAccountToothbrush(mac)))

        val service = mockServiceProvider()

        val toothbrushForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac)).thenReturn(toothbrushForgetSubject)

        val observer = toothbrushForgetter.forgetToothbrush(mac).test().assertNotComplete()

        assertTrue(toothbrushForgetSubject.hasObservers())

        toothbrushForgetSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `forgetToothbrush runs all hooks after toothbrush is forgotten`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac = "mac"
        whenever(toothbrushRepository.getAccountToothbrush(mac))
            .thenReturn(Maybe.just(createAccountToothbrush(mac)))
        val service = mockServiceProvider()

        val toothbrushForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac)).thenReturn(toothbrushForgetSubject)

        val observer = toothbrushForgetter.forgetToothbrush(mac).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        toothbrushForgetSubject.onComplete()

        observer.assertNotComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook2.completableSubject.onComplete()

        observer.assertNotComplete()

        hook1.completableSubject.onComplete()

        observer.assertComplete()

        assertTrue(hook2.completableRun)
        assertTrue(hook1.completableRun)
    }

    @Test
    fun `forgetToothbrush runs all hooks after toothbrush is forgotten, even if one of them throws error`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac = "mac"
        whenever(toothbrushRepository.getAccountToothbrush(mac))
            .thenReturn(Maybe.just(createAccountToothbrush(mac)))
        val service = mockServiceProvider()

        val toothbrushForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac)).thenReturn(toothbrushForgetSubject)

        val observer = toothbrushForgetter.forgetToothbrush(mac).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        toothbrushForgetSubject.onComplete()

        observer.assertNotComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook1.completableSubject.onError(TestForcedException())

        observer.assertNotComplete()

        hook2.completableSubject.onComplete()

        observer.assertError(TestForcedException::class.java)

        assertTrue(hook2.completableRun)
        assertFalse(hook1.completableRun)
    }

    @Test
    fun `forgetToothbrush errors with UnknownToothbrushException if toothbrushRepository doesn't return a toothbrush`() {
        initWithHooks()

        whenever(toothbrushRepository.getAccountToothbrush(any()))
            .thenReturn(Maybe.empty())
        mockServiceProvider()

        toothbrushForgetter.forgetToothbrush("bla").test()
            .assertError { it is UnknownToothbrushException }
    }

    /*
    forgetOwnedByProfile
     */
    @Test
    fun `forgetOwnedByProfile does nothing if profile doesn't own any toothbrush`() {
        initWithHooks()

        mockProfileOwnedToothbrushes()
        val service = mockServiceProvider()

        toothbrushForgetter.forgetOwnedByProfile(ProfileBuilder.DEFAULT_ID).test().assertComplete()

        verify(service, never()).forgetCompletable(any())
    }

    @Test
    fun `forgetOwnedByProfile subscribes to service forgetCompletable for every owned toothbrush`() {
        initWithHooks()

        val mac1 = "mac"
        val mac2 = "tata"
        val toothbrushes = listOf(
            createAccountToothbrush(mac1),
            createAccountToothbrush(mac2)
        )
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)
        val service = mockServiceProvider()

        val toothbrush1ForgetSubject = CompletableSubject.create()
        val toothbrush2ForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac1)).thenReturn(toothbrush1ForgetSubject)
        whenever(service.forgetCompletable(mac2)).thenReturn(toothbrush2ForgetSubject)

        val observer = toothbrushForgetter.forgetOwnedByProfile(ProfileBuilder.DEFAULT_ID).test()

        assertTrue(toothbrush1ForgetSubject.hasObservers())
        assertTrue(toothbrush2ForgetSubject.hasObservers())

        toothbrush1ForgetSubject.onComplete()
        toothbrush2ForgetSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun `forgetOwnedByProfile executes forgetCompletable for every owned toothbrush, even if first emits error`() {
        initWithHooks()

        val mac1 = "mac"
        val mac2 = "tata"
        val toothbrushes = listOf(
            createAccountToothbrush(mac1),
            createAccountToothbrush(mac2)
        )
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)
        val service = mockServiceProvider()

        val toothbrush1ForgetSubject = CompletableSubject.create()
        val toothbrush2ForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac1)).thenReturn(toothbrush1ForgetSubject)
        whenever(service.forgetCompletable(mac2)).thenReturn(toothbrush2ForgetSubject)

        val observer = toothbrushForgetter.forgetOwnedByProfile(ProfileBuilder.DEFAULT_ID).test()

        toothbrush1ForgetSubject.onError(TestForcedException())
        assertTrue(toothbrush2ForgetSubject.hasObservers())

        observer.assertNotComplete()

        toothbrush2ForgetSubject.onComplete()

        observer.assertError(TestForcedException::class.java)

        assertTrue(toothbrush2ForgetSubject.hasComplete())
    }

    @Test
    fun `forgetOwnedByProfile runs all hooks after toothbrush is forgotten`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac1 = "mac"
        val toothbrushes = listOf(createAccountToothbrush(mac1))
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)
        val service = mockServiceProvider()

        val toothbrush1ForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac1)).thenReturn(toothbrush1ForgetSubject)

        val observer = toothbrushForgetter.forgetOwnedByProfile(ProfileBuilder.DEFAULT_ID).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        toothbrush1ForgetSubject.onComplete()

        observer.assertNotComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook2.completableSubject.onComplete()

        observer.assertNotComplete()

        hook1.completableSubject.onComplete()

        observer.assertComplete()

        assertTrue(hook2.completableRun)
        assertTrue(hook1.completableRun)
    }

    @Test
    fun `forgetOwnedByProfile runs all hooks after toothbrush is forgotten, even if one of them throws error`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac1 = "mac"
        val toothbrushes = listOf(createAccountToothbrush(mac1))
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)
        val service = mockServiceProvider()

        val toothbrush1ForgetSubject = CompletableSubject.create()
        whenever(service.forgetCompletable(mac1)).thenReturn(toothbrush1ForgetSubject)

        val observer = toothbrushForgetter.forgetOwnedByProfile(ProfileBuilder.DEFAULT_ID).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        toothbrush1ForgetSubject.onComplete()

        observer.assertNotComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook1.completableSubject.onError(TestForcedException())

        observer.assertNotComplete()

        hook2.completableSubject.onComplete()

        observer.assertError(TestForcedException::class.java)

        assertTrue(hook2.completableRun)
        assertFalse(hook1.completableRun)
    }

    @Test
    fun `eraseToothbrushes call the removeAccountToothbrush from the repository for every owned toothbrush`() {
        initWithHooks()

        val mac1 = "mac"
        val mac2 = "tata"
        val accountToothbrush1 = createAccountToothbrush(mac1)
        val accountToothbrush2 = createAccountToothbrush(mac2)
        val toothbrushes = listOf(
            accountToothbrush1,
            accountToothbrush2
        )
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)

        whenever(toothbrushRepository.removeAccountToothbrush(any()))
            .thenReturn(Completable.complete())

        val observer = toothbrushForgetter.eraseToothbrushes(toothbrushes).test()

        observer.assertComplete()

        verify(toothbrushRepository).removeAccountToothbrush(accountToothbrush1)
        verify(toothbrushRepository).removeAccountToothbrush(accountToothbrush2)
    }

    @Test
    fun `eraseToothbrushes runs all hooks after toothbrush is forgotten`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()
        val removeToothbrushCompletable = CompletableSubject.create()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac1 = "mac"
        val accountToothbrush = createAccountToothbrush(mac1)
        val toothbrushes = listOf(accountToothbrush)
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)

        whenever(toothbrushRepository.removeAccountToothbrush(accountToothbrush))
            .thenReturn(removeToothbrushCompletable)

        val observer = toothbrushForgetter.eraseToothbrushes(toothbrushes).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        observer.assertNotComplete()

        removeToothbrushCompletable.onComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook2.completableSubject.onComplete()

        observer.assertNotComplete()

        hook1.completableSubject.onComplete()

        observer.assertComplete()

        assertTrue(hook2.completableRun)
        assertTrue(hook1.completableRun)
    }

    @Test
    fun `eraseToothbrushes runs all hooks after toothbrush is forgotten, even if one of them throws error`() {
        val hook1 = TestToothbrushForgottenHook()
        val hook2 = TestToothbrushForgottenHook()
        val removeToothbrushCompletable = CompletableSubject.create()

        initWithHooks(forgottenHooks = setOf(hook1, hook2))

        val mac1 = "mac"
        val accountToothbrush = createAccountToothbrush(mac1)
        val toothbrushes = listOf(accountToothbrush)
        mockProfileOwnedToothbrushes(profileToothbrushes = toothbrushes)

        whenever(toothbrushRepository.removeAccountToothbrush(accountToothbrush))
            .thenReturn(removeToothbrushCompletable)

        val observer = toothbrushForgetter.eraseToothbrushes(toothbrushes).test()

        assertFalse(hook1.completableSubject.hasObservers())
        assertFalse(hook2.completableSubject.hasObservers())

        observer.assertNotComplete()

        removeToothbrushCompletable.onComplete()

        assertTrue(hook1.completableSubject.hasObservers())
        assertTrue(hook2.completableSubject.hasObservers())

        hook1.completableSubject.onError(TestForcedException())

        observer.assertNotComplete()

        hook2.completableSubject.onComplete()

        observer.assertError(TestForcedException::class.java)

        assertTrue(hook2.completableRun)
        assertFalse(hook1.completableRun)
    }

    /*
    Utils
     */
    private fun mockProfileOwnedToothbrushes(
        profileId: Long = ProfileBuilder.DEFAULT_ID,
        profileToothbrushes: List<AccountToothbrush> = listOf()
    ) {
        whenever(toothbrushRepository.listAllWithProfileIdOnceAndStream(profileId))
            .thenReturn(BehaviorProcessor.createDefault(profileToothbrushes))
    }

    private fun mockServiceProvider(): KolibreeService {
        val service = mock<KolibreeService>()

        whenever(serviceProvider.connectOnce()).thenReturn(Single.just(service))

        return service
    }

    private fun initWithHooks(forgottenHooks: Set<ToothbrushForgottenHook> = setOf()) {
        toothbrushForgetter =
            ToothbrushForgetterImpl(
                serviceProvider,
                toothbrushRepository,
                forgottenHooks
            )
    }
}

internal class TestToothbrushForgottenHook : ToothbrushForgottenHook {
    var completableRun = false

    val completableSubject = CompletableSubject.create()

    override fun onForgottenCompletable(toothbrush: ForgottenToothbrush): Completable =
        completableSubject.doOnComplete { completableRun = true }
}
