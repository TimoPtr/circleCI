package com.kolibree.sdkws.profile.persistence.repo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.network.utils.FileDownloader
import com.kolibree.sdkws.KolibreeUtils
import com.kolibree.sdkws.api.ConnectivityApiManager
import com.kolibree.sdkws.core.AvatarCache
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.ProfileInternalMapper
import com.kolibree.sdkws.profile.ProfileManagerImpl
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class ProfileManagerImplFunctionalTest : BaseProfileTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var profileRepository: ProfileRepository

    private lateinit var profileManager: ProfileManagerImpl

    private val kolibreeUtils = mock<KolibreeUtils>()
    private val avatarCache = mock<AvatarCache>()

    private val fileDownloader = mock<FileDownloader>()

    private val connectivityManager = mock<ConnectivityApiManager>()
    private val profileApi = mock<ProfileApi>()

    @Before
    override fun setUp() {
        super.setUp()
        initRoom()
        whenever(connectivityManager.hasConnectivity()).thenReturn(true)
        profileRepository = ProfileRepositoryImpl(profileDatastore, connectivityManager, profileApi)
        profileManager =
            ProfileManagerImpl(
                apiRoomDatabase.offlineUpdateDao(),
                kolibreeUtils,
                fileDownloader,
                avatarCache,
                profileRepository,
                ProfileInternalMapper()
            )
    }

    @Test
    fun verifyCanUpdateAProfileLocally() {

        val newName = "userWithNewName"
        val newGender = genderFemale
        val firstProfile = profiles.first()
        val newProfile = firstProfile.copy(firstName = newName, gender = newGender)

        profileDatastore.addProfile(firstProfile)

        profileManager.updateOrInsertProfileLocally(newProfile).concatWith {
            profileDatastore.getProfile(firstProfile.id)
                .test()
                .assertNoErrors()
                .assertValue(newProfile)
        }
    }

    @Test
    fun verifyCanDeleteAProfileLocally() {

        val firstProfile = profiles.first()

        profileManager.deleteProfileLocally(firstProfile.id).concatWith {
            profileDatastore.getProfile(firstProfile.id)
                .test()
                .assertError(NoSuchElementException())
        }
    }

    @Test
    fun verifyCanCreateProfile() {

        val firstProfile = profiles.first()
        val data = createProfileData()

        whenever(profileApi.createProfile(defaultAccountId, data))
            .thenReturn(Single.just(Response.success(firstProfile)))

        profileManager.createProfile(data, defaultAccountId)
            .test()
            .assertNoErrors()
            .assertValue(firstProfile.copy(transitionSounds = true))
    }

    @Test
    fun verifyCanUpdateAProfile() {

        val newName = "userWithNewName"
        val newGender = genderFemale
        val firstProfile = profiles.first()
        val newProfile = firstProfile.copy(firstName = newName, gender = newGender)

        profileDatastore.addProfile(firstProfile)

        whenever(profileApi.updateProfile(anyLong(), anyLong(), any<ProfileInternal>()))
            .thenReturn(Single.just(Response.success(newProfile)))

        profileManager.updateProfile(newProfile.accountId.toLong(), newProfile.exportProfile())
            .map {
                assertEquals(newProfile, it)
                profileDatastore.getProfile(it.id).test()
                    .assertNoErrors()
                    .assertValue(newProfile)
            }
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearAll()
    }
}
