package com.kolibree.sdkws.core

import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.test.CommonBaseTest
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore
import com.kolibree.sdkws.profile.persistence.repo.ProfileRepository
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import junit.framework.TestCase.assertNull
import org.junit.Test

class ProfileWrapperTest : CommonBaseTest() {
    private val connector: InternalKolibreeConnector = mock()
    private val brushingsRepository: BrushingsRepository = mock()
    private val profileRepository: ProfileRepository = mock()
    private val goPirateDatastore: GoPirateDatastore = mock()
    private val profile: ProfileInternal = mock()

    companion object {
        private const val DEFAULT_PROFILE_ID = 5L
    }

    private lateinit var profileWrapper: ProfileWrapper

    override fun setup() {
        super.setup()

        whenever(profile.id).thenReturn(DEFAULT_PROFILE_ID)

        whenever(profileRepository.getProfileLocally(DEFAULT_PROFILE_ID)).thenReturn(Single.just(profile))

        profileWrapper = ProfileWrapper(
            DEFAULT_PROFILE_ID,
            connector,
            brushingsRepository,
            profileRepository,
            goPirateDatastore
        )
    }

    @Test
    fun setCurrent_invokesKolibreeConnectorSetActiveProfile() {
        profileWrapper.setCurrent()

        verify(connector).setActiveProfile(DEFAULT_PROFILE_ID)
    }

    @Test
    fun `getLastBrushingSession returns null on RuntimeException`() {
        whenever(brushingsRepository.getLastBrushingSession(DEFAULT_PROFILE_ID)).thenAnswer {
            throw RuntimeException("Test forced exception")
        }

        assertNull(profileWrapper.lastBrushingSession)
    }

    @Test
    fun `createBrushingSync invokes connector addBrushingSync`() {
        val data = mock<CreateBrushingData>()
        doNothing().whenever(connector).addBrushing(data, profile)

        profileWrapper.createBrushingSync(data)

        verify(connector).addBrushingSync(data, profile)
    }

    @Test
    fun `createBrushingSingle invokes connector addBrushingSingle`() {
        val data = mock<CreateBrushingData>()
        val brushing = mock<Brushing>()
        doReturn(Single.just(brushing)).whenever(connector).addBrushingSingle(data, profile)

        val observer = profileWrapper.createBrushingSingle(data).test()

        observer.assertValue(brushing)
        verify(connector).addBrushingSingle(data, profile)
    }
}
