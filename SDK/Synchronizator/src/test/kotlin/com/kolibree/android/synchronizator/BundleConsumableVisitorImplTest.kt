package com.kolibree.android.synchronizator

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizableKey.ACCOUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILES
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import com.kolibree.android.synchronizator.network.Consumable
import com.kolibree.android.synchronizator.network.SynchronizeAccountResponse
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class BundleConsumableVisitorImplTest : BaseUnitTest() {
    private val visitor = spy(BundleConsumableVisitorImpl())

    /*
    VISIT SYNCHRONIZABLE ITEM BUNDLE
     */

    @Test
    fun `visit SynchronizableItemBundle with syncAccountResponse without key for bundle returns null`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableItemBundle>()
        doReturn(null).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        assertNull(visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableItemBundle with ItemConsumable for bundle returns ItemBundleConsumable`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableItemBundle>()
        whenever(bundle.dataStore).thenReturn(mock())
        val itemConsumable = mock<Consumable>()
        doReturn(itemConsumable).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        val expectedItemBundleConsumable = ItemBundleConsumable(bundle, itemConsumable)
        assertEquals(expectedItemBundleConsumable, visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableItemBundle with ItemConsumable for bundle invokes dataStore updateVersion`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableItemBundle>()
        val datastore = mock<SynchronizableItemDataStore>()
        whenever(bundle.dataStore).thenReturn(datastore)
        val expectedVersion = 6
        val itemConsumable = Consumable(expectedVersion)
        doReturn(itemConsumable).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        visitor.visit(syncAccountResponse, bundle)

        verify(datastore).updateVersion(expectedVersion)
    }

    /*
    VISIT SYNCHRONIZABLE READ ONLY BUNDLE
     */

    @Test
    fun `visit SynchronizableReadOnlyBundle with syncAccountResponse without key for bundle returns null`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableReadOnlyBundle>()
        doReturn(null).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        assertNull(visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableReadOnlyBundle with ReadOnlyConsumable for bundle returns ReadOnlyBundleConsumable`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableReadOnlyBundle>()
        whenever(bundle.dataStore).thenReturn(mock())
        val readOnlyConsumable = mock<Consumable>()
        doReturn(readOnlyConsumable).whenever(visitor)
            .consumableForBundle(syncAccountResponse, bundle)

        val expectedReadOnlyBundleConsumable = ReadOnlyBundleConsumable(bundle, readOnlyConsumable)
        assertEquals(expectedReadOnlyBundleConsumable, visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableReadOnlyBundle with ReadOnlyConsumable for bundle invokes dataStore updateVersion`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableReadOnlyBundle>()
        val datastore = mock<SynchronizableReadOnlyDataStore>()
        whenever(bundle.dataStore).thenReturn(datastore)
        val expectedVersion = 6
        val itemConsumable = Consumable(expectedVersion)
        doReturn(itemConsumable).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        visitor.visit(syncAccountResponse, bundle)

        verify(datastore).updateVersion(expectedVersion)
    }

    /*
    VISIT SYNCHRONIZABLE CATALOG BUNDLE
     */

    @Test
    fun `visit SynchronizableCatalogBundle with syncAccountResponse without key for bundle returns null`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableCatalogBundle>()
        doReturn(null).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        assertNull(visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableCatalogBundle with syncAccountResponse with key for bundle returns ReadOnlyBundleConsumable`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableCatalogBundle>()
        whenever(bundle.dataStore).thenReturn(mock())
        val catalogConsumable = mock<Consumable>()
        doReturn(catalogConsumable).whenever(visitor)
            .consumableForBundle(syncAccountResponse, bundle)

        val expectedCatalogBundleConsumable = CatalogBundleConsumable(bundle, catalogConsumable)
        assertEquals(expectedCatalogBundleConsumable, visitor.visit(syncAccountResponse, bundle))
    }

    @Test
    fun `visit SynchronizableCatalogBundle with CatalogConsumable for bundle invokes dataStore updateVersion`() {
        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        val bundle = mock<SynchronizableCatalogBundle>()
        val datastore = mock<SynchronizableCatalogDataStore>()
        whenever(bundle.dataStore).thenReturn(datastore)
        val expectedVersion = 6
        val itemConsumable = Consumable(expectedVersion)
        doReturn(itemConsumable).whenever(visitor).consumableForBundle(syncAccountResponse, bundle)

        visitor.visit(syncAccountResponse, bundle)

        verify(datastore).updateVersion(expectedVersion)
    }

    /*
    CONSUMABLE FOR BUNDLE
     */
    @Test
    fun `consumableForBundle returns null if typeConsumable is empty`() {
        val bundle = mockBundleWithKey(PROFILES)

        val syncAccountResponse = SynchronizeAccountResponse(mapOf())

        assertNull(visitor.consumableForBundle(syncAccountResponse, bundle))
    }

    @Test
    fun `consumableForBundle returns null if bundle key not in typeConsumable`() {
        val bundle = mockBundleWithKey(ACCOUNT)

        val syncAccountResponse =
            SynchronizeAccountResponse(mapOf(PROFILES to mock()))

        assertNull(visitor.consumableForBundle(syncAccountResponse, bundle))
    }

    @Test
    fun `consumableForBundle returns consumable if bundle key in typeConsumable`() {
        val bundleKey = PROFILES
        val bundle = mockBundleWithKey(bundleKey)

        val expectedConsumable = mock<Consumable>()
        val syncAccountResponse = SynchronizeAccountResponse(mapOf(bundleKey to expectedConsumable))

        assertEquals(expectedConsumable, visitor.consumableForBundle(syncAccountResponse, bundle))
    }

    private fun mockBundleWithKey(bundleKey: SynchronizableKey): Bundle {
        val keyBuilder = object : SynchronizeAccountKeyBuilder(bundleKey) {
            override fun version(): Int {
                TODO("test shouldn't touch this method")
            }
        }
        val bundle = mock<Bundle>()
        whenever(bundle.synchronizeAccountKeyBuilder).thenReturn(keyBuilder)

        return bundle
    }
}
