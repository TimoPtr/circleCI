package com.kolibree.sdkws.profile.persistence.repo

import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.sdkws.api.ConnectivityApiManager
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.models.PictureResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryImplTest : BaseUnitTest() {

    private lateinit var profileRepository: ProfileRepositoryImpl

    private val profileApi = mock<ProfileApi>()
    private val profileDatastore = mock<ProfileDatastore>()

    private val connectivityApiManager = mock<ConnectivityApiManager>()

    @Before
    fun setUp() {
        profileRepository =
            spy(ProfileRepositoryImpl(profileDatastore, connectivityApiManager, profileApi))
    }

    /*
    DELETE PROFILE
     */

    @Test
    fun deleteProfile_withConnectivity_apiSucceeds_invokesDataStoreDelete() {
        whenever(connectivityApiManager.hasConnectivity()).thenReturn(true)

        val accountId = 5L
        val profileId = 9L

        val response = mock<ResponseBody>()
        whenever(profileApi.deleteProfile(accountId, profileId))
            .thenReturn(Single.just(Response.success(response)))

        whenever(profileDatastore.deleteProfile(profileId)).thenReturn(Completable.complete())

        profileRepository.deleteProfile(accountId, profileId).test().assertValue(true)

        verify(profileDatastore).deleteProfile(profileId)
    }

    /*
    UPDATE PROFILE
     */

    @Test
    fun updateProfile_apiSucceds_invokesDataStoreUpdate() {
        whenever(connectivityApiManager.hasConnectivity()).thenReturn(true)

        val accountId = 5L
        val profileId = 9L
        val profileInternal = mock<ProfileInternal>()
        whenever(profileInternal.id).thenReturn(profileId)

        val response = mock<ProfileInternal>()
        whenever(profileApi.updateProfile(accountId, profileId, profileInternal))
            .thenReturn(Single.just(Response.success(response)))

        whenever(profileDatastore.updateProfile(response)).thenReturn(Completable.complete())

        profileRepository.updateProfile(accountId, profileInternal).test()
            .assertValue(response)

        verify(profileDatastore).updateProfile(response)
    }

    @Test
    fun updateProfile3Params_apiSucceds_neverInvokesDataStoreUpdate() {
        whenever(connectivityApiManager.hasConnectivity()).thenReturn(true)

        val accountId = 5L
        val profileId = 9L
        val editProfileData = mock<EditProfileData>()

        val response = mock<ResponseBody>()
        whenever(profileApi.updateProfile(accountId, profileId, editProfileData))
            .thenReturn(Single.just(Response.success(response)))

        profileRepository.updateProfile(accountId, profileId, editProfileData).test()
            .assertValue(true)

        verify(profileDatastore, never()).updateProfile(any())
    }

    /*
    INSERT PROFILE LOCALLY
     */

    @Test
    fun insertProfileLocally_callsDataStoreAddMethod() {
        val profile = mock<ProfileInternal>()
        profileRepository.insertProfileLocally(profile)
        verify(profileDatastore).addProfile(profile)
    }

    @Test
    fun insertProfileLocally_returnsAQueriedInstance() {
        val profileDatabaseId = 1986L
        val profileAdded = mock<ProfileInternal>()
        val profileDatabase = mock<ProfileInternal>()
        whenever(profileDatabase.id).thenReturn(profileDatabaseId)
        whenever(profileDatastore.addProfile(any())).thenReturn(profileDatabaseId)
        whenever(profileDatastore.getProfile(eq(profileDatabaseId)))
            .thenReturn(Single.just(profileDatabase))
        val profileGot = profileRepository.insertProfileLocally(profileAdded).blockingGet()
        assertEquals(profileDatabaseId, profileGot.id)
    }

    @Test
    fun getProfilePicture_callsProfileApi() {
        val accountId = 5L
        val profileId = 9L

        val mockResponse = mock<PictureResponse>()
        whenever(profileApi.getProfilePicture(accountId, profileId))
            .thenReturn(Single.just(Response.success(mockResponse)))

        profileRepository.getProfilePicture(accountId, profileId).test().assertValue(mockResponse)
    }
}
