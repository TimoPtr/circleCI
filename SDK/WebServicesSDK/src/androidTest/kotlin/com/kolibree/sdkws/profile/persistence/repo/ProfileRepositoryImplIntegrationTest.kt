package com.kolibree.sdkws.profile.persistence.repo

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.network.utils.NetworkChecker
import com.kolibree.sdkws.api.ConnectivityApiManagerImpl
import com.kolibree.sdkws.brushing.RetrofitHelperTest
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.core.SynchronizationScheduler
import com.kolibree.sdkws.data.model.CreateProfileData
import com.kolibree.sdkws.data.model.EditProfileData
import com.kolibree.sdkws.profile.ProfileApi
import com.kolibree.sdkws.profile.utils.MockProfileApi
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileRepositoryImplIntegrationTest : BaseProfileTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val networkChecker = mock<NetworkChecker>()
    private val synchronizationScheduler = mock<SynchronizationScheduler>()
    private val connectivityManager =
        ConnectivityApiManagerImpl(synchronizationScheduler, networkChecker)
    private lateinit var profileRepository: ProfileRepository

    @Before
    override fun setUp() {
        super.setUp()
        whenever(connectivityManager.hasConnectivity()).thenReturn(true)
        val delegate = RetrofitHelperTest().generateDelegate<ProfileApi>()
        val mockProfileApi = MockProfileApi(delegate, profiles)
        val prfileDatastore = mock<ProfileDatastore>()
        profileRepository =
            ProfileRepositoryImpl(prfileDatastore, connectivityManager, mockProfileApi)
    }

    @Test
    fun deleteProfileForAGivenUserWithoutConnectivity() {
        initConnectivity(false)
        val profile =
            createProfileInternal(DEFAULT_PROFILE_ID, firstNameProfile1, genderMale, pointsProfile1, ageProfile1)
        profileRepository.deleteProfile(profile.accountId.toLong(), profile.id).networkTest()
    }

    @Test
    fun updateProfileForAGivenUserWithoutConnectivity() {
        initConnectivity(false)
        val profile =
            createProfileInternal(DEFAULT_PROFILE_ID, firstNameProfile1, genderMale, pointsProfile1, ageProfile1)
        profileRepository.updateProfile(defaultAccountId, profile).networkTest()
    }

    @Test
    fun updateProfileWithoutConnectivity() {
        initConnectivity(false)
        profileRepository.updateProfile(defaultAccountId, 42, EditProfileData().also {
            it.brushingTime = 0
        }).networkTest()
    }

    @Test
    fun createProfileForAGivenUserWithoutConnectivity() {
        initConnectivity(false)
        profileRepository.createProfile(CreateProfileData(), defaultAccountId).networkTest()
    }

    @Test
    fun deleteProfileForAGivenUserWithConnectivity() {
        initConnectivity(true)
        val profile =
            createProfileInternal(DEFAULT_PROFILE_ID, firstNameProfile1, genderMale, pointsProfile1, ageProfile1)

        profileRepository.createProfile(createProfileData(), defaultAccountId).map {
            profileRepository.deleteProfile(profile.accountId.toLong(), it.id)
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue(true)
        }
    }

    @Test
    fun updateProfileForAGivenUserWithConnectivity() {
        initConnectivity(true)
        val profile =
            createProfileInternal(DEFAULT_PROFILE_ID, firstNameProfile1, genderMale, pointsProfile1, ageProfile1)

        profileRepository.createProfile(createProfileData(), defaultAccountId).map {

            profileRepository.updateProfile(defaultAccountId, profile.copy(id = it.id))
                .test()
                .assertComplete()
                .assertNoErrors()
                .assertValue { it.compare(profile) }
        }
    }

    @Test
    fun updateProfileWithConnectivity() {
        initConnectivity(true)
        val profile = EditProfileData()
        profile.brushingTime = IBrushing.MINIMUM_BRUSHING_GOAL_TIME_SECONDS
        profile.countryCode = "FR"
        profile.handedness = "M"
        profile.age = ageProfile1.toInt()
        profile.gender = genderMale
        profile.firstName = firstNameProfile1

        profileRepository.updateProfile(defaultAccountId, 42, profile)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(true)
    }

    @Test
    fun createProfileForAGivenUserWithConnectivity() {
        initConnectivity(true)
        profileRepository.createProfile(createProfileData(), defaultAccountId)
            .test()
            .assertComplete()
            .assertNoErrors()
    }

    private fun initConnectivity(hasConnectivity: Boolean) {
        whenever(networkChecker.hasConnectivity()).thenReturn(hasConnectivity)
    }

    private fun <T> Single<T>.networkTest() {
        this.test().assertError { it.message!!.contains("Network unavailable") }
    }
}
