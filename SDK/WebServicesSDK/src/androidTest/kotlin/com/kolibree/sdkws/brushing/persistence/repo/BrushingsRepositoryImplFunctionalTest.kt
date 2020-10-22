package com.kolibree.sdkws.brushing.persistence.repo

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.commons.interfaces.LocalBrushingsProcessor
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.kolibree.sdkws.brushing.BrushingApiManager
import com.kolibree.sdkws.brushing.DEFAULT_ACCOUNT_ID
import com.kolibree.sdkws.brushing.PROFILE_ID_USER1
import com.kolibree.sdkws.brushing.createBrushingData
import com.kolibree.sdkws.brushing.createBrushingInternal
import com.kolibree.sdkws.brushing.createProfile
import com.kolibree.sdkws.brushing.generateBrushingResponse
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.brushing.utils.BrushingHelperTest
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.exception.AlreadySavedBrushingException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import java.net.SocketTimeoutException
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.nullable
import retrofit2.HttpException

@RunWith(AndroidJUnit4::class)
class BrushingsRepositoryImplFunctionalTest : BrushingHelperTest() {

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    private val brushingManager = mock<BrushingApiManager>()
    private val profileDatastore = mock<ProfileDatastore>()
    private val localBrushingsProcessor: LocalBrushingsProcessor = mock()

    private lateinit var brushingsRepository: BrushingsRepository
    private lateinit var profile: ProfileInternal
    private lateinit var brushingData: CreateBrushingData
    private lateinit var brushingInternal: BrushingInternal

    @Before
    override fun setUp() {
        super.setUp()
        initRoom()
        brushingsRepository =
            BrushingsRepositoryImpl(
                brushingManager,
                brushingsDatastore,
                profileDatastore,
                localBrushingsProcessor
            )
        profile = createProfile()
        brushingData = createBrushingData()
        brushingInternal = BrushingInternal.fromBrushingData(brushingData, PROFILE_ID_USER1)
    }

    @After
    override fun tearDown() {
        super.tearDown()
        clearAll()
    }

    @Test
    fun verifyAddBrushingWhenSucceed() {
        whenever(brushingManager.createBrushing(anyLong(), anyLong(), any()))
            .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

        brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
            .map { expected ->

                brushingsDatastore.getBrushings(PROFILE_ID_USER1).test()
                    .await()
                    .assertNoErrors()
                    .assertValue { res ->
                        res.map { it.extractBrushing() }.contains(expected)
                    }
            }
    }

    @Test
    fun verifyCannotAddDuplicateBrushing() {
        whenever(brushingManager.createBrushing(anyLong(), anyLong(), any()))
            .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

        brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
            .map {
                brushingsRepository.addBrushing(brushingData, profile, profile.accountId.toLong())
                    .test()
                    .assertComplete()
                    .assertError(AlreadySavedBrushingException())
            }
    }

    @Test
    fun verifyAddBrushingWhenError400() {
        val error = mock<HttpException>()
        whenever(error.code()).thenReturn(400)
        whenever(brushingManager.createBrushing(anyLong(), anyLong(), any()))
            .thenReturn(Single.error(error))
        brushingsRepository.addBrushing(brushingData, profile, DEFAULT_ACCOUNT_ID)
        brushingsDatastore.getBrushings(PROFILE_ID_USER1).test()
            .assertNoErrors()
            .assertValue { res ->
                !res.contains(brushingInternal)
            }
    }

    @Test
    fun verifyAddBrushingWhenThrowTimeout() {

        whenever(brushingManager.createBrushing(anyLong(), anyLong(), any()))
            .thenReturn(Single.error(SocketTimeoutException()))
        brushingsRepository.addBrushing(brushingData, profile, DEFAULT_ACCOUNT_ID)
        brushingsDatastore.getBrushings(PROFILE_ID_USER1).test()
            .assertNoErrors()
            .assertValue { res ->
                !res.contains(brushingInternal)
            }
    }

    @Test
    fun verifyBrushingsAreSynchronizedCorrectly() {
        addDeletedData()
        whenever(
            brushingManager.getBrushingsInDateRange(
                eq(DEFAULT_ACCOUNT_ID),
                eq(PROFILE_ID_USER1),
                any(),
                any(),
                nullable(Int::class.java)
            )
        )
            .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

        whenever(brushingManager.createBrushings(anyLong(), anyLong(), anyList()))
            .thenReturn(Single.just(generateBrushingResponse(PROFILE_ID_USER1)))

        whenever(brushingManager.deleteBrushings(anyLong(), anyLong(), anyList()))
            .thenReturn(Single.just(true))

        brushingsRepository.synchronizeBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1).blockingGet()

        val res = brushingsDatastore.getDeletedLocally().blockingGet()
        Assert.assertEquals(emptyList<Brushing>(), res)
    }

    /*
    DELETE BRUSHING
     */

    @Test
    fun deleteBrushing_emptyBrushingId_brushingDeleted() {
        val accountId = 12L
        val profileId = 34L
        val brushingId = 0L
        val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

        assertBrushingInDatastore(profileId, brushing)
        brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing)
            .blockingAwait()
        assertNonDeletedLocallyBrushingInDatastore()
    }

    @Test
    fun deleteBrushing_notEmptyBrushingId_requestFailed_brushingDeletedLocally() {
        val accountId = 12L
        val profileId = 34L
        val brushingId = 45L
        val brushing = mockForDeleteBrushing(accountId, profileId, brushingId, false)

        assertBrushingInDatastore(profileId, brushing)
        brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing)
            .blockingAwait()
        assertOneDeletedLocallyBrushingInDatastore(brushing)
    }

    @Test
    fun deleteBrushing_notEmptyBrushingId_requestSuccess_brushingDeletedLocally() {
        val accountId = 12L
        val profileId = 34L
        val brushingId = 45L
        val brushing = mockForDeleteBrushing(accountId, profileId, brushingId)

        assertBrushingInDatastore(profileId, brushing)
        brushingsRepository.deleteBrushing(accountId, profileId, brushing = brushing)
            .blockingAwait()
        assertNonDeletedLocallyBrushingInDatastore()
    }

    /*
    BRUSHINGS FLOWABLE
     */

    @Test
    fun brushingsFlowable_dataStoreEmitsNewList_emitsNewList() {
        val profileId = 34L

        val brushingsProcessor = PublishProcessor.create<List<BrushingInternal>>()
        val brushingDataStore = mock<BrushingsDatastore>()
        whenever(brushingDataStore.brushingsFlowable(profileId)).thenReturn(brushingsProcessor)

        val repository =
            BrushingsRepositoryImpl(
                brushingManager, brushingDataStore, profileDatastore,
                this.localBrushingsProcessor
            )
        val observer = repository.brushingsFlowable(profileId).test()

        observer.assertValueCount(0)

        brushingsProcessor.onNext(listOf())

        observer.assertValueCount(1).assertNotComplete()
    }

    /*
    UTILS
     */

    private fun mockForDeleteBrushing(
        accountId: Long,
        profileId: Long,
        brushingId: Long,
        requestResult: Boolean = true
    ): Brushing {
        val internal =
            createBrushingInternal(profileId = profileId, minusDay = 0L, kolibreeId = brushingId)
        val brushing = internal.extractBrushing()
        whenever(
            brushingManager.deleteBrushing(
                accountId,
                profileId,
                brushingId
            )
        ).thenReturn(Single.just(requestResult))
        brushingsDatastore.addBrushingIfDoNotExist(internal)
        return brushing
    }

    private fun assertBrushingInDatastore(profileId: Long, brushing: Brushing) {
        brushingsDatastore.getBrushings(profileId).test()
            .assertNoErrors()
            .assertComplete()
            .assertValue {
                it.first().compare(brushing)
            }
    }

    private fun assertOneDeletedLocallyBrushingInDatastore(brushing: Brushing) {
        brushingsDatastore.getDeletedLocally().test()
            .assertNoErrors()
            .assertComplete()
            .assertValue {
                it.first().compare(brushing)
            }
    }

    private fun assertNonDeletedLocallyBrushingInDatastore() =
        Assert.assertEquals(0, brushingsDatastore.getDeletedLocally().blockingGet().size)
}
