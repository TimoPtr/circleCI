package com.kolibree.android.sdk.connection.user

import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.commons.ToothbrushModel.ARA
import com.kolibree.android.commons.ToothbrushModel.CONNECT_B1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E1
import com.kolibree.android.commons.ToothbrushModel.CONNECT_E2
import com.kolibree.android.commons.ToothbrushModel.CONNECT_M1
import com.kolibree.android.commons.ToothbrushModel.PLAQLESS
import com.kolibree.android.sdk.connection.user.UserBaseImpl.Companion.NO_CACHE_PROFILE_ID
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** [UserBaseImpl] unit tests */
class UserBaseImplTest : BaseUnitTest() {

    private lateinit var userBaseImpl: UserBaseImpl

    /*
    Companion
     */

    @Test
    fun `Companion's NO_CACHE_PROFILE_ID constant value is -1L`() {
        assertEquals(-1L, NO_CACHE_PROFILE_ID)
    }

    @Test
    fun `NO_CACHE_PROFILE_ID will never be equal to SHARED_MODE_PROFILE_ID`() {
        assertNotEquals(SHARED_MODE_PROFILE_ID, NO_CACHE_PROFILE_ID)
    }

    /*
    profileId()
     */

    @Test
    fun `When running in bootloader mode, profileId() emits a CommandNotSupportedException`() {
        userBaseImpl = UserBaseImplStub(bootloader = true)
        userBaseImpl.profileId().test().assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `When the toothbrush is shared, profileId() emits ToothbrushInSharedModeException`() {
        userBaseImpl = UserBaseImplStub()
        userBaseImpl.profileIdCache.set(SHARED_MODE_PROFILE_ID)
        userBaseImpl.profileId().test().assertError(ToothbrushInSharedModeException::class.java)
    }

    @Test
    fun `When the profile ID cache is not set and queryProfileIdAndUpdateCache() emits 1, profileId() emits ToothbrushInSharedModeException`() {
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.profileIdCache.set(NO_CACHE_PROFILE_ID)

        doReturn(Single.just(SHARED_MODE_PROFILE_ID)).whenever(userBaseImpl).queryProfileIdAndUpdateCache()

        userBaseImpl.profileId().test().assertError(ToothbrushInSharedModeException::class.java)
    }

    @Test
    fun `When the profile ID cache is not set and queryProfileIdAndUpdateCache() emits an id different than 1, profileId() emits that Id`() {
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.profileIdCache.set(NO_CACHE_PROFILE_ID)

        val expectedId = 543L
        doReturn(Single.just(expectedId)).whenever(userBaseImpl).queryProfileIdAndUpdateCache()

        userBaseImpl.profileId().test().assertValue(expectedId)
    }

    @Test
    fun `When a profile ID cache is set, profileId() returns it without querying the toothbrush`() {
        val profileId = 1986L
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.profileIdCache.set(profileId)
        userBaseImpl.profileId().test().assertValue(profileId)
        verify(userBaseImpl, never()).queryProfileIdAndUpdateCache()
    }

    /*
    isSharedModeEnabled()
     */

    @Test
    fun `When the profile ID is SHARED_MODE_PROFILE_ID, then isSharedModeEnabled() emits true`() {
        userBaseImpl = UserBaseImplStub()
        userBaseImpl.profileIdCache.set(SHARED_MODE_PROFILE_ID)
        userBaseImpl.isSharedModeEnabled().test().assertValue(true)
    }

    @Test
    fun `When the profile ID is a real one, then isSharedModeEnabled() emits false`() {
        userBaseImpl = UserBaseImplStub()
        userBaseImpl.profileIdCache.set(1986L)
        userBaseImpl.isSharedModeEnabled().test().assertValue(false)
    }

    /*
    setProfileId()
     */

    @Test
    fun `When running in bootloader, setProfileId() emits CommandNotSupportedException`() {
        userBaseImpl = UserBaseImplStub(bootloader = true)
        userBaseImpl.setProfileId(1L).test().assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `When called with same ID, setProfileId() doesn't call sendProfileIdAndUpdateCache()`() {
        val profileId = 1986L
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.profileIdCache.set(profileId)
        userBaseImpl.setProfileId(profileId).test()
        verify(userBaseImpl, never()).sendProfileIdAndUpdateCache(any())
    }

    @Test
    fun `When called with a different ID, setProfileId() calls sendProfileIdAndUpdateCache()`() {
        val newProfileId = 1986L
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.profileIdCache.set(1987L)
        userBaseImpl.setProfileId(newProfileId).test()
        verify(userBaseImpl).sendProfileIdAndUpdateCache(newProfileId)
    }

    /*
    enableSharedMode()
     */

    @Test
    fun `For a E1, enableSharedMode() calls setProfileId() with SHARED_MODE_PROFILE_ID`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = CONNECT_E1))
        userBaseImpl.enableSharedMode().test()
        verify(userBaseImpl).setProfileId(SHARED_MODE_PROFILE_ID)
    }

    @Test
    fun `For an Ara, enableSharedMode() calls setProfileId() with SHARED_MODE_PROFILE_ID`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = ARA))
        userBaseImpl.enableSharedMode().test()
        verify(userBaseImpl).setProfileId(SHARED_MODE_PROFILE_ID)
    }

    @Test
    fun `For a E2, enableSharedMode() calls setProfileId() with SHARED_MODE_PROFILE_ID`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = CONNECT_E2))
        userBaseImpl.enableSharedMode().test()
        verify(userBaseImpl).setProfileId(SHARED_MODE_PROFILE_ID)
    }

    @Test
    fun `For a M1, enableSharedMode() emits ToothbrushNotShareableException`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = CONNECT_M1))
        userBaseImpl.enableSharedMode().test()
            .assertError(ToothbrushNotShareableException::class.java)
    }

    @Test
    fun `For a B1, enableSharedMode() calls setProfileId() with SHARED_MODE_PROFILE_ID`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = CONNECT_B1))
        userBaseImpl.enableSharedMode().test()
        verify(userBaseImpl).setProfileId(SHARED_MODE_PROFILE_ID)
    }

    @Test
    fun `For a PQL, enableSharedMode() calls setProfileId() with SHARED_MODE_PROFILE_ID`() {
        userBaseImpl = spy(UserBaseImplStub(toothbrushModel = PLAQLESS))
        userBaseImpl.enableSharedMode().test()
        verify(userBaseImpl).setProfileId(SHARED_MODE_PROFILE_ID)
    }

    /*
    clearCache()
     */

    @Test
    fun `clearCache() resets the profile ID cache to NO_CACHE_PROFILE_ID`() {
        userBaseImpl = UserBaseImplStub()
        userBaseImpl.profileIdCache.set(1989L)
        userBaseImpl.clearCache()
        assertEquals(NO_CACHE_PROFILE_ID, userBaseImpl.profileIdCache.get())
    }

    /*
    queryProfileIdAndUpdateCache()
     */

    @Test
    fun `queryProfileIdAndUpdateCache() gets toothbrush's profile ID, caches it and emits it`() {
        val toothbrushProfileId = 1983L
        userBaseImpl = spy(UserBaseImplStub(profileId = toothbrushProfileId))
        userBaseImpl.queryProfileIdAndUpdateCache().test().assertValue(toothbrushProfileId)
        assertEquals(toothbrushProfileId, userBaseImpl.profileIdCache.get())
        verify(userBaseImpl).getToothbrushProfileId()
    }

    /*
    sendProfileIdAndUpdateCache()
     */

    @Test
    fun `sendProfileIdAndUpdateCache() sets toothbrush's profile ID then caches it`() {
        val toothbrushProfileId = 1983L
        userBaseImpl = spy(UserBaseImplStub())
        userBaseImpl.sendProfileIdAndUpdateCache(toothbrushProfileId).test().assertComplete()
        assertEquals(toothbrushProfileId, userBaseImpl.profileIdCache.get())
        verify(userBaseImpl).sendProfileIdAndUpdateCache(toothbrushProfileId)
    }

    /*
    profileOrSharedModeId
     */

    @Test
    fun `profileOrSharedModeId emits profileId()'s output when the device is not in shared mode`() {
        val expectedProfileId = 1986L
        userBaseImpl = spy(UserBaseImplStub())
        whenever(userBaseImpl.profileId()).doReturn(Single.just(expectedProfileId))

        userBaseImpl
            .profileOrSharedModeId()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(expectedProfileId)
    }

    @Test
    fun `profileOrSharedModeId emits SHARED_MODE_PROFILE_ID when the device is in shared mode`() {
        userBaseImpl = spy(UserBaseImplStub())
        whenever(userBaseImpl.profileId())
            .doReturn(Single.error<Long>(ToothbrushInSharedModeException()))

        userBaseImpl
            .profileOrSharedModeId()
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(SHARED_MODE_PROFILE_ID)
    }

    @Test
    fun `profileOrSharedModeId emits non ToothbrushInSharedModeException errors`() {
        val expectedError = RuntimeException()
        userBaseImpl = spy(UserBaseImplStub())
        whenever(userBaseImpl.profileId()).doReturn(Single.error<Long>(expectedError))

        userBaseImpl
            .profileOrSharedModeId()
            .test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(expectedError)
    }
}

internal class UserBaseImplStub(
    toothbrushModel: ToothbrushModel = CONNECT_E1,
    private var profileId: Long = 1L,
    private val bootloader: Boolean = false
) : UserBaseImpl(toothbrushModel) {

    override fun setToothbrushProfileId(profileId: Long) {
        this.profileId = profileId
    }

    override fun getToothbrushProfileId(): Long {
        return profileId
    }

    override fun isToothbrushRunningBootloader(): Boolean {
        return bootloader
    }
}
