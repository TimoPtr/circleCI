/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.sdkws.brushing

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.interfaces.RemoteBrushingsProcessor
import com.kolibree.android.processedbrushings.CheckupCalculator
import com.kolibree.sdkws.api.ConnectivityApiManagerImpl
import com.kolibree.sdkws.brushing.models.BrushingApiModel
import com.kolibree.sdkws.brushing.models.BrushingsResponse
import com.kolibree.sdkws.brushing.models.CreateMultipleBrushingSessionsBody
import com.kolibree.sdkws.brushing.persistence.models.BrushingInternal
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.DeleteBrushingData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.Test
import retrofit2.Response

@ExperimentalCoroutinesApi
class BrushingApiManagerImplTest : BaseUnitTest() {
    private val brushingApi: BrushingApi = mock()
    private val connectivityApiManagerImpl: ConnectivityApiManagerImpl = mock()
    private val checkupCalculator: CheckupCalculator = mock()

    private var remoteBrushingsProcessor: RemoteBrushingsProcessor? = mock()

    private lateinit var apiManager: BrushingApiManagerImpl

    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun setup() {
        super.setup()
        Dispatchers.setMain(mainThreadSurrogate)

        init()
    }

    override fun tearDown() {
        super.tearDown()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

    /*
    DELETE SINGLE BRUSHING
     */

    @Test
    fun `deleteBrushing invokes brushing api create brushing when there's connectivity`() {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        val expectedBrushingId = 5L
        apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, expectedBrushingId)

        verify(brushingApi).deleteBrushing(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            DeleteBrushingData(listOf(expectedBrushingId))
        )
    }

    @Test
    fun `deleteBrushing never invokes syncWhenConnectivityAvailable when there's connectivity`() {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L)

        verify(
            connectivityApiManagerImpl,
            never()
        ).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `deleteBrushing invokes onBrushingsDeleted on success`() = runBlockingTest {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        val observer = apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L).test()

        observer.assertComplete()

        assertOnBrushingsRemovedInvoked()
    }

    @Test
    fun `deleteBrushing doesn't crash on success if remoteBrushingsProcess is null`() =
        runBlockingTest {
            initNullRemoteBrushingsProcessor()

            mockWithConnectivity()
            mockDeleteBrushingApi()

            val observer =
                apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L).test()

            observer.assertComplete()
        }

    @Test
    fun `deleteBrushing never invokes onBrushingsDeleted onError`() = runBlockingTest {
        mockWithConnectivity()

        val expectedError = Exception("Test forced error")
        whenever(brushingApi.deleteBrushing(any(), any(), any())).thenReturn(
            Single.error(
                expectedError
            )
        )

        val observer = apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L).test()

        observer.assertError(expectedError)

        assertOnBrushingsRemovedNotInvoked()
    }

    @Test
    fun `deleteBrushing invokes syncWhenConnectivityAvailable when there's no connectivity`() {
        mockWithConnectivity(hasConnectivity = false)

        apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L).test()

        verify(connectivityApiManagerImpl).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `deleteBrushing never invokes onBrushingsDeleted when there's no connectivity`() =
        runBlockingTest {
            mockWithConnectivity(hasConnectivity = false)

            apiManager.deleteBrushing(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, 5L).test()

            assertOnBrushingsRemovedNotInvoked()
        }

    /*
    DELETE MULTIPLE BRUSHINGS
     */

    @Test
    fun `deleteBrushings invokes brushing api create brushing when there's connectivity`() {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        val expectedKLId1 = 54L
        val expectedKLId2 = 124L
        val brushing1 = createBrushing(kolibreeId = expectedKLId1)
        val brushing2 = createBrushing(kolibreeId = expectedKLId2)

        apiManager.deleteBrushings(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            listOf(brushing1, brushing2)
        )

        verify(brushingApi).deleteBrushing(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            DeleteBrushingData(listOf(expectedKLId1, expectedKLId2))
        )
    }

    @Test
    fun `deleteBrushings never invokes syncWhenConnectivityAvailable when there's connectivity`() {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        apiManager.deleteBrushings(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, listOf(createBrushing()))

        verify(
            connectivityApiManagerImpl,
            never()
        ).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `deleteBrushings invokes onBrushingsDeleted on success`() = runBlockingTest {
        mockWithConnectivity()
        mockDeleteBrushingApi()

        val observer = apiManager.deleteBrushings(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            listOf(createBrushing())
        ).test()

        observer.assertComplete()

        assertOnBrushingsRemovedInvoked()
    }

    @Test
    fun `deleteBrushings doesn't crash on success if remoteBrushingsProcessor is null `() =
        runBlockingTest {
            initNullRemoteBrushingsProcessor()

            mockWithConnectivity()
            mockDeleteBrushingApi()

            val observer = apiManager.deleteBrushings(
                DEFAULT_ACCOUNT_ID,
                PROFILE_ID_USER1,
                listOf(createBrushing())
            ).test()

            observer.assertComplete()
        }

    @Test
    fun `deleteBrushings never invokes onBrushingsDeleted onError`() = runBlockingTest {
        mockWithConnectivity()

        val expectedError = Exception("Test forced error")
        whenever(brushingApi.deleteBrushing(any(), any(), any())).thenReturn(
            Single.error(
                expectedError
            )
        )

        val observer = apiManager.deleteBrushings(
            DEFAULT_ACCOUNT_ID,
            PROFILE_ID_USER1,
            listOf(createBrushing())
        ).test()

        observer.assertError(expectedError)

        assertOnBrushingsRemovedNotInvoked()
    }

    @Test
    fun `deleteBrushings invokes syncWhenConnectivityAvailable when there's no connectivity`() {
        mockWithConnectivity(hasConnectivity = false)

        apiManager.deleteBrushings(DEFAULT_ACCOUNT_ID, PROFILE_ID_USER1, listOf(createBrushing()))
            .test()

        verify(connectivityApiManagerImpl).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `deleteBrushings never invokes onBrushingsDeleted when there's no connectivity`() =
        runBlockingTest {
            mockWithConnectivity(hasConnectivity = false)

            apiManager.deleteBrushings(
                    DEFAULT_ACCOUNT_ID,
                    PROFILE_ID_USER1,
                    listOf(createBrushing())
                )
                .test()

            assertOnBrushingsRemovedNotInvoked()
        }

    /*
    CREATE SINGLE BRUSHING
     */

    @Test
    fun `createBrushing invokes brushing api create brushing when there's connectivity`() {
        mockWithConnectivity()
        mockCreateMultipleBrushingSApi()

        val brushingInternal = mock<BrushingInternal>()
        val expectedData = mock<CreateBrushingData>()
        whenever(brushingInternal.extractCreateBrushingData(checkupCalculator)).thenReturn(
            expectedData
        )

        apiManager.createBrushing(DEFAULT_ACCOUNT_ID, brushingInternal.profileId, brushingInternal)

        verify(brushingApi).createBrushings(
            DEFAULT_ACCOUNT_ID,
            brushingInternal.profileId,
            CreateMultipleBrushingSessionsBody(listOf(expectedData))
        )
    }

    @Test
    fun `createBrushing never invokes syncWhenConnectivityAvailable when there's connectivity`() {
        mockWithConnectivity()
        mockCreateMultipleBrushingSApi()

        val brushingInternal = mock<BrushingInternal>()
        val expectedData = mock<CreateBrushingData>()
        whenever(brushingInternal.extractCreateBrushingData(checkupCalculator)).thenReturn(
            expectedData
        )

        apiManager.createBrushing(DEFAULT_ACCOUNT_ID, brushingInternal.profileId, brushingInternal)

        verify(
            connectivityApiManagerImpl,
            never()
        ).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `createBrushing doesn't crash on success if remoteBrushingsProcessorIsNull`() =
        runBlockingTest {
            initNullRemoteBrushingsProcessor()

            mockWithConnectivity()
            mockCheckupCalculator()

            val brushingInternal = createBrushingInternal()

            mockCreateMultipleBrushingSApi()

            val observer =
                apiManager.createBrushing(
                    DEFAULT_ACCOUNT_ID,
                    brushingInternal.profileId,
                    brushingInternal
                ).test()

            observer.assertComplete()
        }

    @Test
    fun `createBrushing invokes onBrushingsCreated on success`() = runBlockingTest {
        mockWithConnectivity()
        mockCheckupCalculator()

        val brushingInternal = createBrushingInternal()

        mockCreateMultipleBrushingSApi()

        val observer =
            apiManager.createBrushing(
                DEFAULT_ACCOUNT_ID,
                brushingInternal.profileId,
                brushingInternal
            ).test()

        observer.assertComplete()

        assertOnBrushingsCreatedInvoked()
    }

    @Test
    fun `createBrushing never invokes onBrushingsCreated onError`() = runBlockingTest {
        mockWithConnectivity()
        mockCheckupCalculator()

        val brushingInternal = createBrushingInternal()

        val expectedError = Exception("Test forced error")
        whenever(brushingApi.createBrushings(any(), any(), any()))
            .thenReturn(Single.error(expectedError))

        val observer =
            apiManager.createBrushing(
                DEFAULT_ACCOUNT_ID,
                brushingInternal.profileId,
                brushingInternal
            ).test()

        observer.assertError(expectedError)

        assertOnBrushingsCreatedNotInvoked()
    }

    @Test
    fun `createBrushing invokes syncWhenConnectivityAvailable when there's no connectivity`() {
        mockWithConnectivity(hasConnectivity = false)

        val brushingInternal = createBrushingInternal()

        apiManager.createBrushing(DEFAULT_ACCOUNT_ID, brushingInternal.profileId, brushingInternal)
            .test()

        verify(connectivityApiManagerImpl).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `createBrushing never invokes onBrushingsCreated when there's no connectivity`() =
        runBlockingTest {
            mockWithConnectivity(hasConnectivity = false)

            val brushingInternal = createBrushingInternal()

            apiManager.createBrushing(
                    DEFAULT_ACCOUNT_ID,
                    brushingInternal.profileId,
                    brushingInternal
                )
                .test()

            assertOnBrushingsCreatedNotInvoked()
        }

    /*
    CREATE MULTIPLE BRUSHING
     */

    @Test
    fun `createBrushings invokes brushing api create brushing when there's connectivity`() {
        mockWithConnectivity()
        mockCreateMultipleBrushingSApi()

        val brushingInternal1 = mock<BrushingInternal>()
        val expectedData1 = mock<CreateBrushingData>()
        whenever(brushingInternal1.extractCreateBrushingData(checkupCalculator)).thenReturn(
            expectedData1
        )

        val brushingInternal2 = mock<BrushingInternal>()
        val expectedData2 = mock<CreateBrushingData>()
        whenever(brushingInternal2.extractCreateBrushingData(checkupCalculator)).thenReturn(
            expectedData2
        )

        apiManager.createBrushings(
            DEFAULT_ACCOUNT_ID,
            brushingInternal1.profileId,
            listOf(brushingInternal1, brushingInternal2)
        )

        verify(brushingApi).createBrushings(
            DEFAULT_ACCOUNT_ID,
            brushingInternal1.profileId,
            CreateMultipleBrushingSessionsBody(listOf(expectedData1, expectedData2))
        )
    }

    @Test
    fun `createBrushings never invokes syncWhenConnectivityAvailable when there's connectivity`() {
        mockWithConnectivity()
        mockCreateMultipleBrushingSApi()

        val brushingInternal = mock<BrushingInternal>()
        val expectedData = mock<CreateBrushingData>()
        whenever(brushingInternal.extractCreateBrushingData(checkupCalculator)).thenReturn(
            expectedData
        )

        apiManager.createBrushings(
            DEFAULT_ACCOUNT_ID,
            brushingInternal.profileId,
            listOf(brushingInternal)
        )

        verify(
            connectivityApiManagerImpl,
            never()
        ).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `createBrushings invokes onBrushingsCreated on success`() = runBlockingTest {
        mockWithConnectivity()
        mockCheckupCalculator()
        mockCreateMultipleBrushingSApi()

        val brushingInternal = createBrushingInternal()

        val observer =
            apiManager.createBrushings(
                DEFAULT_ACCOUNT_ID,
                brushingInternal.profileId,
                listOf(brushingInternal)
            ).test()

        observer.assertComplete()

        assertOnBrushingsCreatedInvoked()
    }

    @Test
    fun `createBrushings doesn't crash on success if remoteBrushingsProcessor is null `() =
        runBlockingTest {
            initNullRemoteBrushingsProcessor()

            mockWithConnectivity()
            mockCheckupCalculator()
            mockCreateMultipleBrushingSApi()

            val brushingInternal = createBrushingInternal()

            val observer =
                apiManager.createBrushings(
                    DEFAULT_ACCOUNT_ID,
                    brushingInternal.profileId,
                    listOf(brushingInternal)
                ).test()

            observer.assertComplete()
        }

    @Test
    fun `createBrushings never invokes onBrushingsCreated onError`() = runBlockingTest {
        mockWithConnectivity()
        mockCheckupCalculator()

        val brushingInternal = createBrushingInternal()

        val expectedError = Exception("Test forced error")
        whenever(brushingApi.createBrushings(any(), any(), any())).thenReturn(
            Single.error(
                expectedError
            )
        )

        val observer =
            apiManager.createBrushings(
                DEFAULT_ACCOUNT_ID,
                brushingInternal.profileId,
                listOf(brushingInternal)
            ).test()

        observer.assertError(expectedError)

        assertOnBrushingsCreatedNotInvoked()
    }

    @Test
    fun `createBrushings invokes syncWhenConnectivityAvailable when there's no connectivity`() {
        mockWithConnectivity(hasConnectivity = false)

        val brushingInternal = createBrushingInternal()

        apiManager.createBrushings(
            DEFAULT_ACCOUNT_ID,
            brushingInternal.profileId,
            listOf(brushingInternal)
        ).test()

        verify(connectivityApiManagerImpl).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `createBrushings never invokes onBrushingsCreated when there's no connectivity`() =
        runBlockingTest {
            mockWithConnectivity(hasConnectivity = false)

            val brushingInternal = createBrushingInternal()

            apiManager.createBrushings(
                DEFAULT_ACCOUNT_ID,
                brushingInternal.profileId,
                listOf(brushingInternal)
            ).test()

            assertOnBrushingsCreatedNotInvoked()
        }

    /*
    ASSIGN BRUSHINGS
     */

    @Test
    fun `assignBrushings invokes brushing api create brushing when there's connectivity`() {
        mockWithConnectivity()
        mockAssignBrushingsApi()

        val brushing = createBrushing()

        apiManager.assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(brushing)
        )

        val expectedBrushingApiModel = BrushingApiModel(
            brushing.goalDuration,
            brushing.kolibreeId,
            brushing.processedData
        )

        verify(brushingApi).assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(expectedBrushingApiModel)
        )
    }

    @Test
    fun `assignBrushings never invokes syncWhenConnectivityAvailable when there's connectivity`() {
        mockWithConnectivity()
        mockAssignBrushingsApi()

        val brushing = createBrushing()

        apiManager.assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(brushing)
        )

        verify(
            connectivityApiManagerImpl,
            never()
        ).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `assignBrushings invokes onBrushingsCreated on success`() = runBlockingTest {
        mockWithConnectivity()
        mockAssignBrushingsApi()

        val brushing = createBrushing()

        val observer = apiManager.assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(brushing)
        ).test()

        observer.assertComplete()

        assertOnBrushingsCreatedInvoked()
    }

    @Test
    fun `assignBrushings doesn't crash on success if remoteBrushingsProcessor is null`() =
        runBlockingTest {
            initNullRemoteBrushingsProcessor()

            mockWithConnectivity()
            mockAssignBrushingsApi()

            val brushing = createBrushing()

            val observer = apiManager.assignBrushings(
                DEFAULT_ACCOUNT_ID,
                brushing.profileId,
                listOf(brushing)
            ).test()

            observer.assertComplete()
        }

    @Test
    fun `assignBrushings never invokes onBrushingsCreated onError`() = runBlockingTest {
        mockWithConnectivity()

        val brushing = createBrushing()

        val expectedError = Exception("Test forced error")
        whenever(brushingApi.assignBrushings(any(), any(), any())).thenReturn(
            Single.error(
                expectedError
            )
        )

        val observer = apiManager.assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(brushing)
        ).test()

        observer.assertError(expectedError)

        assertOnBrushingsCreatedNotInvoked()
    }

    @Test
    fun `assignBrushings invokes syncWhenConnectivityAvailable when there's no connectivity`() {
        mockWithConnectivity(hasConnectivity = false)

        val brushing = createBrushing()

        apiManager.assignBrushings(
            DEFAULT_ACCOUNT_ID,
            brushing.profileId,
            listOf(brushing)
        ).test()

        verify(connectivityApiManagerImpl).syncWhenConnectivityAvailable<BrushingsResponse>()
    }

    @Test
    fun `assignBrushings never invokes onBrushingsCreated when there's no connectivity`() =
        runBlockingTest {
            mockWithConnectivity(hasConnectivity = false)

            val brushing = createBrushing()

            apiManager.assignBrushings(
                DEFAULT_ACCOUNT_ID,
                brushing.profileId,
                listOf(brushing)
            ).test()

            assertOnBrushingsCreatedNotInvoked()
        }

    /*
    UTILS
     */

    private fun mockCreateMultipleBrushingSApi() {
        val multipleBrushingResponse = Single.just(Response.success(BrushingsResponse(listOf())))
        whenever(brushingApi.createBrushings(any(), any(), any())).thenReturn(
            multipleBrushingResponse
        )
    }

    private fun mockAssignBrushingsApi() {
        whenever(brushingApi.assignBrushings(any(), any(), any())).thenReturn(Single.just(mock()))
    }

    private fun mockDeleteBrushingApi() {
        whenever(brushingApi.deleteBrushing(any(), any(), any())).thenReturn(Single.just(mock()))
    }

    private fun mockWithConnectivity(hasConnectivity: Boolean = true) {
        whenever(connectivityApiManagerImpl.hasConnectivity()).thenReturn(hasConnectivity)

        if (!hasConnectivity) {
            val any = mock<Any>()
            whenever(connectivityApiManagerImpl.syncWhenConnectivityAvailable<Any>()).thenReturn(
                Single.just(any)
            )
        }
    }

    private fun mockCheckupCalculator() {
        whenever(checkupCalculator.calculateCheckup(anyOrNull<String>(), any(), any())).thenReturn(
            mock()
        )
    }

    private fun init() {
        apiManager =
            BrushingApiManagerImpl(
                brushingApi,
                connectivityApiManagerImpl,
                checkupCalculator,
                remoteBrushingsProcessor,
                CoroutineScope(Dispatchers.Unconfined)
            )
    }

    private fun initNullRemoteBrushingsProcessor() {
        remoteBrushingsProcessor = null

        init()
    }

    private suspend fun assertOnBrushingsCreatedInvoked() {
        verify(remoteBrushingsProcessor!!).onBrushingsCreated()
    }

    private suspend fun assertOnBrushingsCreatedNotInvoked() {
        verify(remoteBrushingsProcessor!!, never()).onBrushingsCreated()
    }

    private suspend fun assertOnBrushingsRemovedInvoked() {
        verify(remoteBrushingsProcessor!!).onBrushingsRemoved()
    }

    private suspend fun assertOnBrushingsRemovedNotInvoked() {
        verify(remoteBrushingsProcessor!!, never()).onBrushingsRemoved()
    }
}
