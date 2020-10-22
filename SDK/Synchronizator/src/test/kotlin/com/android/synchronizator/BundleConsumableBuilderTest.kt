package com.android.synchronizator

import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.BundleConsumable
import com.kolibree.android.synchronizator.BundleConsumableBuilderImpl
import com.kolibree.android.synchronizator.BundleConsumableVisitor
import com.kolibree.android.synchronizator.CatalogBundleConsumable
import com.kolibree.android.synchronizator.ItemBundleConsumable
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.models.SynchronizableKey.ACCOUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILES
import com.kolibree.android.synchronizator.models.SynchronizeAccountKey
import com.kolibree.android.synchronizator.network.SynchronizeAccountApi
import com.kolibree.android.synchronizator.network.SynchronizeAccountRequestBody
import com.kolibree.android.synchronizator.network.SynchronizeAccountResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import java.io.IOException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mock
import retrofit2.Call
import retrofit2.Response

internal class BundleConsumableBuilderTest : BaseUnitTest() {
    @Mock
    lateinit var synchronizeAccountApi: SynchronizeAccountApi

    @Mock
    lateinit var accountDatastore: AccountDatastore

    @Mock
    lateinit var bundleConsumableVisitor: BundleConsumableVisitor

    private lateinit var bundleConsumableBuilder: BundleConsumableBuilderImpl

    override fun setup() {
        super.setup()

        bundleConsumableBuilder =
            BundleConsumableBuilderImpl(
                synchronizeAccountApi,
                accountDatastore,
                bundleConsumableVisitor
            )

        SynchronizationBundles.clear()
    }

    override fun tearDown() {
        super.tearDown()

        SynchronizationBundles.clear()
    }

    /*
    BUILD BUNDLE CONSUMABLES
     */
    @Test
    fun buildBundleConsumables_nullAccount_returnsEmptyList() {
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.empty())

        assertTrue(bundleConsumableBuilder.buildBundleConsumables().isEmpty())
    }

    @Test
    fun buildBundleConsumables_withAccount_executeException_returnsEmptyList() {
        val account: AccountInternal = mock()
        val expectedId = 5L
        whenever(account.id).thenReturn(expectedId)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))
        val call = mock<Call<SynchronizeAccountResponse>>()
        whenever(call.execute()).thenThrow(IOException("Test forcedError"))
        whenever(synchronizeAccountApi.synchronizationInfo(eq(expectedId), any())).thenReturn(call)

        assertTrue(bundleConsumableBuilder.buildBundleConsumables().isEmpty())
    }

    @Test
    fun buildBundleConsumables_withAccount_executeSuccess_invokesProcessSyncAccountResponse() {
        val account: AccountInternal = mock()
        val expectedAccountId = 5L
        whenever(account.id).thenReturn(expectedAccountId)
        whenever(accountDatastore.getAccountMaybe()).thenReturn(Maybe.just(account))
        val call = mock<Call<SynchronizeAccountResponse>>()
        val expectedResponse = Response.success(mock<SynchronizeAccountResponse>())
        whenever(call.execute()).thenReturn(expectedResponse)
        whenever(
            synchronizeAccountApi.synchronizationInfo(
                eq(expectedAccountId),
                any()
            )
        ).thenReturn(call)

        bundleConsumableBuilder = spy(bundleConsumableBuilder)

        val key1 = SynchronizeAccountKey(PROFILES, 1)
        val key2 = SynchronizeAccountKey(ACCOUNT, 2)
        val body = SynchronizeAccountRequestBody(setOf(key1, key2))
        doReturn(body).whenever(bundleConsumableBuilder).buildSynchronizeAccountRequestBody()

        val expectedParameterMap: Map<String, Int> = mapOf(
            key1.key.value to key1.version,
            key2.key.value to key2.version
        )

        doReturn(listOf<BundleConsumable>()).whenever(bundleConsumableBuilder)
            .processSyncAccountResponse(any())

        bundleConsumableBuilder.buildBundleConsumables()

        verify(synchronizeAccountApi).synchronizationInfo(expectedAccountId, expectedParameterMap)

        verify(bundleConsumableBuilder).processSyncAccountResponse(expectedResponse)
    }

    /*
    BUILD SYNCHRONIZE ACCOUNT REQUEST BODY
     */
    @Test
    fun buildSynchronizeAccountRequestBody_emptyBundles_returnsEmptyRequest() {
        assertEquals(
            SynchronizeAccountRequestBody(setOf()),
            bundleConsumableBuilder.buildSynchronizeAccountRequestBody()
        )
    }

    @Test
    fun buildSynchronizeAccountRequestBody_withOneBundle_returnsRequestWithSingleKeyAndVersion() {
        SynchronizationBundles.register(synchronizableItemBundle())

        assertEquals(
            SynchronizeAccountRequestBody(setOf(synchronizeAccountKeyBuilder().build())),
            bundleConsumableBuilder.buildSynchronizeAccountRequestBody()
        )
    }

    @Test
    fun buildSynchronizeAccountRequestBody_withMultipleBundle_returnsRequestWithMultipleKeyAndVersion() {
        SynchronizationBundles.register(synchronizableCatalogBundle())
        val secondKey = ACCOUNT
        val secondVersion = 78
        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                    key = secondKey,
                    returnVersion = secondVersion
                )
            )
        )

        assertEquals(
            SynchronizeAccountRequestBody(
                setOf(
                    synchronizeAccountKeyBuilder(
                        key = secondKey,
                        returnVersion = secondVersion
                    ).build(),
                    synchronizeAccountKeyBuilder().build()
                )
            ),
            bundleConsumableBuilder.buildSynchronizeAccountRequestBody()
        )
    }

    /*
    PROCESS SYNC ACCOUNT RESPONSE
     */
    @Test
    fun processSyncAccountResponse_nonSuccessfulResponse_returnsEmptyList() {
        val response: Response<SynchronizeAccountResponse> =
            Response.error(404, ResponseBody.create(null, "ignored"))
        assertTrue(bundleConsumableBuilder.processSyncAccountResponse(response).isEmpty())
    }

    @Test
    fun processSyncAccountResponse_successfulResponse_nullBody_returnsEmptyList() {
        val response: Response<SynchronizeAccountResponse> = Response.success(null)

        bundleConsumableBuilder = spy(bundleConsumableBuilder)

        assertTrue(bundleConsumableBuilder.processSyncAccountResponse(response).isEmpty())
    }

    @Test
    fun processSyncAccountResponse_successfulResponse_returnsEmptyList() {
        val expectedBody = SynchronizeAccountResponse(mapOf())
        val response: Response<SynchronizeAccountResponse> = Response.success(expectedBody)

        bundleConsumableBuilder = spy(bundleConsumableBuilder)

        bundleConsumableBuilder.processSyncAccountResponse(response)

        verify(bundleConsumableBuilder).processResponseBody(expectedBody)
    }

    /*
    PROCESS RESPONSE BODY
     */
    @Test
    fun processResponseBody_emptyBundles_returnsEmptyList() {
        assertTrue(
            bundleConsumableBuilder.processResponseBody(
                SynchronizeAccountResponse(
                    mapOf()
                )
            ).isEmpty()
        )

        verifyNoMoreInteractions(bundleConsumableVisitor)
    }

    @Test
    fun `processResponseBody invokes accept on each bundle`() {
        val bundle1 = spy(synchronizableCatalogBundle())
        val bundle2 = spy(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                    key = ACCOUNT
                )
            )
        )

        SynchronizationBundles.register(bundle1)
        SynchronizationBundles.register(bundle2)

        val syncAccountResponse = SynchronizeAccountResponse(mapOf())
        bundleConsumableBuilder.processResponseBody(syncAccountResponse)

        verify(bundle1).accept(bundleConsumableVisitor, syncAccountResponse)
        verify(bundle2).accept(bundleConsumableVisitor, syncAccountResponse)
    }

    @Test
    fun `processResponseBody returns list with consumables returned by accept`() {
        val bundle1 = synchronizableCatalogBundle()
        val bundle2 = synchronizableItemBundle(
            synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                key = ACCOUNT
            )
        )
        SynchronizationBundles.register(bundle1)
        SynchronizationBundles.register(bundle2)

        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val expectedItemConsumable = mock<ItemBundleConsumable>()
        val expectedCatalogConsumable = mock<CatalogBundleConsumable>()
        whenever(bundleConsumableVisitor.visit(syncAccountResponse, bundle1)).thenReturn(
            expectedCatalogConsumable
        )
        whenever(bundleConsumableVisitor.visit(syncAccountResponse, bundle2)).thenReturn(
            expectedItemConsumable
        )

        val consumableList = bundleConsumableBuilder.processResponseBody(syncAccountResponse)

        assertEquals(2, consumableList.size)

        assertTrue(consumableList.contains(expectedCatalogConsumable))
        assertTrue(consumableList.contains(expectedItemConsumable))
    }

    @Test
    fun `processResponseBody ignores null BundleConsumables returned by accept`() {
        val bundle1 = synchronizableCatalogBundle()
        val bundle2 = synchronizableItemBundle(
            synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(
                key = ACCOUNT
            )
        )
        SynchronizationBundles.register(bundle1)
        SynchronizationBundles.register(bundle2)

        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val expectedItemConsumable = mock<ItemBundleConsumable>()
        whenever(bundleConsumableVisitor.visit(syncAccountResponse, bundle1)).thenReturn(null)
        whenever(bundleConsumableVisitor.visit(syncAccountResponse, bundle1)).thenReturn(
            expectedItemConsumable
        )

        val consumableList = bundleConsumableBuilder.processResponseBody(syncAccountResponse)

        assertEquals(1, consumableList.size)

        assertTrue(consumableList.contains(expectedItemConsumable))
    }
}
