package com.kolibree.android.accountinternal

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.reactivex.processors.ReplayProcessor
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.Mock

class CurrentProfileProviderImplTest : BaseUnitTest() {
    @Mock
    lateinit var accountDatastore: AccountDatastore

    private lateinit var currentProfileProvider: CurrentProfileProviderImpl
    private val accountProcessor = PublishProcessor.create<AccountInternal>()

    override fun setup() {
        super.setup()

        currentProfileProvider = CurrentProfileProviderImpl(accountDatastore)

        setupDatastoreFlowable()
    }

    @Test
    fun `currentProfileFlowable noAccount returnsEmptyFlowable`() {
        currentProfileProvider.currentProfileFlowable().test().assertEmpty().assertNotComplete()
    }

    @Test
    fun `currentProfileFlowable emits empty flowable, completes and invokes reset if accountFlowable emits error`() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertEmpty().assertNotComplete()

        val resetObserver = currentProfileProvider.resetProcessor.test().assertNotComplete()

        accountProcessor.onError(TestForcedException())

        observer.assertError(TestForcedException::class.java)

        resetObserver.assertComplete()
    }

    @Test
    fun `currentProfileFlowable returns new instance if accountFlowable emits error`() {
        val originalFlowable = currentProfileProvider.currentProfileFlowable()

        originalFlowable.test()

        accountProcessor.onError(TestForcedException())

        assertNotEquals(originalFlowable, currentProfileProvider.currentProfileFlowable())
    }

    @Test
    fun `currentProfileFlowable noActiveProfile returnsEmptyFlowable and continues to emit on new account`() {
        val account = mockAccount(currentProfileId = null)

        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertEmpty().assertNotComplete()

        accountProcessor.onNext(account)

        observer.assertEmpty().assertNotComplete()

        val newAccount = mockAccount()

        val expectedProfile: Profile = mockExportProfile(newAccount, newAccount.currentProfileId!!)

        accountProcessor.onNext(newAccount)

        observer.assertValue(expectedProfile).assertNotComplete()
    }

    @Test
    fun `currentProfileFlowable getProfileInternalWithId returns null emitsEmpty and continues to emit on new account`() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L
        val account = mockAccount(currentProfileId)

        assertNull(account.getProfileInternalWithId(currentProfileId))

        accountProcessor.onNext(account)

        observer.assertEmpty().assertNotComplete()

        val expectedProfile: Profile = mockExportProfile(account, account.currentProfileId!!)

        accountProcessor.onNext(account)

        observer.assertValue(expectedProfile).assertNotComplete()
    }

    @Test
    fun `currentProfileFlowable exportProfile throws exception emitsEmpty and continues to emit on new account`() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L
        val account = mockAccount(currentProfileId)

        val profileInternal: ProfileInternal = mock()
        whenever(profileInternal.exportProfile()).thenAnswer { throw TestForcedException() }
        whenever(account.getProfileInternalWithId(currentProfileId)).thenReturn(profileInternal)

        accountProcessor.onNext(account)

        observer.assertEmpty().assertNotComplete()

        val expectedProfile: Profile = mockExportProfile(account, account.currentProfileId!!)

        accountProcessor.onNext(account)

        observer.assertValue(expectedProfile).assertNotComplete()
    }

    @Test
    fun currentProfileFlowable_accountDataStoreEmitsWithProfile_emitsProfile() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L
        val account = mockAccount(currentProfileId)
        val expectedProfile: Profile = mockExportProfile(account, currentProfileId)

        accountProcessor.onNext(account)

        observer.assertValue(expectedProfile).assertNotComplete()
    }

    @Test
    fun currentProfileFlowable_accountDataStoreEmitsWithSameProfileMultipleTimes_emitsProfileOnce() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L

        val account = mockAccount(currentProfileId)

        val expectedProfile: Profile = mockExportProfile(account, currentProfileId)

        accountProcessor.onNext(account)

        observer.assertValues(expectedProfile).assertNotComplete()

        accountProcessor.onNext(account)

        observer.assertValues(expectedProfile).assertNotComplete()
    }

    @Test
    fun currentProfileFlowable_accountDataStoreEmitsWithDifferentProfileMultipleTimes_emitsProfiles() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val firstProfileId = 8786L
        val secondProfileId = firstProfileId + 1

        val account = mockAccount(firstProfileId)

        val firstExpectedProfile: Profile = mockExportProfile(account, firstProfileId)
        val secondExpectedProfile: Profile = mockExportProfile(account, secondProfileId)

        accountProcessor.onNext(account)

        observer.assertValues(firstExpectedProfile).assertNotComplete()

        whenever(account.currentProfileId).thenReturn(secondProfileId)
        accountProcessor.onNext(account)

        observer.assertValues(firstExpectedProfile, secondExpectedProfile).assertNotComplete()
    }

    @Test
    fun currentProfileFlowable_newInstanceIsCreatedOnUnsubscription() {
        val flowable = currentProfileProvider.currentProfileFlowable()

        val observer = flowable.test()

        observer.dispose()

        assertNotEquals(flowable, currentProfileProvider.currentProfileFlowable())
    }

    @Test
    fun currentProfileFlowable_futureSubscribersReceiveLastProfile() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L
        val account = mockAccount(currentProfileId)

        val expectedProfile: Profile = mockExportProfile(account, currentProfileId)

        accountProcessor.onNext(account)

        currentProfileProvider.currentProfileFlowable().test().assertValue(expectedProfile)
            .assertNotComplete()
    }

    @Test
    fun currentProfileFlowable_resetClearsLastEmittedProfile() {
        val observer = currentProfileProvider.currentProfileFlowable().test()

        observer.assertNoErrors().assertNotComplete()

        val currentProfileId = 8786L
        val account = mockAccount(currentProfileId)

        val expectedProfile: Profile = mockExportProfile(account, currentProfileId)

        accountProcessor.onNext(account)

        observer.assertValues(expectedProfile).assertNotComplete()

        currentProfileProvider.reset()

        currentProfileProvider.currentProfileFlowable().test().assertValueCount(0)
            .assertNotComplete()

        observer.assertComplete()
    }

    /*
    currentProfileSingle
     */
    @Test
    fun `currentProfileSingle returns profile from currentProfileFlowable`() {
        spyCurrentProfileProvider()

        val expectedProfile: Profile = mock()
        val currentProfileFlowable = Flowable.just(expectedProfile)
        doReturn(currentProfileFlowable).whenever(currentProfileProvider).currentProfileFlowable()

        currentProfileProvider.currentProfileSingle().test().assertValue(expectedProfile)
    }

    @Test
    fun `currentProfileSingle returns first emitted profile from currentProfileFlowable`() {
        spyCurrentProfileProvider()

        val expectedProfile: Profile = mock()
        val currentProfileFlowable = PublishProcessor.create<Profile>()
        doReturn(currentProfileFlowable).whenever(currentProfileProvider).currentProfileFlowable()

        val observer = currentProfileProvider.currentProfileSingle().test().assertEmpty()

        currentProfileFlowable.onNext(expectedProfile)

        observer.assertValue(expectedProfile)
    }

    @Test
    fun `currentProfileSingle returns NoSuchElementException if currentProfileFlowable completes`() {
        spyCurrentProfileProvider()

        val currentProfileFlowable = Flowable.empty<Profile>()
        doReturn(currentProfileFlowable).whenever(currentProfileProvider).currentProfileFlowable()

        currentProfileProvider.currentProfileSingle().test()
            .assertError(NoSuchElementException::class.java)
    }

    /*
    currentProfile
     */

    @Test
    fun `currentProfile returns Profile if currentProfileFlowable has a single item`() {
        spyCurrentProfileProvider()

        val profile = mock<Profile>()
        val currentProfileFlowable = ReplayProcessor.create<Profile>()
        doReturn(currentProfileFlowable).whenever(currentProfileProvider).currentProfileFlowable()
        currentProfileFlowable.onNext(profile)
        currentProfileFlowable.onComplete()

        assertEquals(profile, currentProfileProvider.currentProfile())
    }

    @Test(expected = NoSuchElementException::class)
    fun `currentProfile throws NoSuchElementException if currentProfileFlowable has no items`() {
        spyCurrentProfileProvider()

        val currentProfileFlowable = Flowable.empty<Profile>()
        doReturn(currentProfileFlowable).whenever(currentProfileProvider).currentProfileFlowable()

        currentProfileProvider.currentProfile()
    }

    /*
    UTILS
     */

    private fun mockAccount(
        currentProfileId: Long? = DEFAULT_PROFILE_ID
    ): AccountInternal {
        val account: AccountInternal = mock()
        whenever(account.currentProfileId).thenReturn(currentProfileId)

        return account
    }

    private fun mockExportProfile(
        account: AccountInternal,
        currentProfileId: Long
    ): Profile {
        val profileInternal: ProfileInternal = mock()
        val expectedProfile: Profile = mock()
        whenever(profileInternal.exportProfile()).thenReturn(expectedProfile)
        whenever(account.getProfileInternalWithId(currentProfileId)).thenReturn(profileInternal)
        return expectedProfile
    }

    private fun setupDatastoreFlowable() {
        whenever(accountDatastore.accountFlowable()).thenReturn(accountProcessor)
    }

    private fun spyCurrentProfileProvider() {
        currentProfileProvider = spy(currentProfileProvider)
    }

    private companion object {
        const val DEFAULT_PROFILE_ID = 5L
    }
}
