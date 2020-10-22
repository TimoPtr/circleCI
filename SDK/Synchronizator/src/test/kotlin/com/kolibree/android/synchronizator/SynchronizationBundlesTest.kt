/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator

import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableTrackingEntity
import com.android.synchronizator.synchronizeAccountKeyBuilder
import com.android.synchronizator.testItemDatastore
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly.overrideDelegateWith
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizableKey.ACCOUNT
import com.kolibree.android.synchronizator.models.SynchronizableKey.CHALLENGE_PROGRESS
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILES
import com.kolibree.android.synchronizator.models.SynchronizableKey.PROFILE_SMILES
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class SynchronizationBundlesTest : BaseUnitTest() {
    override fun setup() {
        super.setup()

        SynchronizationBundles.clear()
    }

    override fun tearDown() {
        super.tearDown()

        SynchronizationBundles.clear()
    }

    @Test
    fun `register adds a Bundle to the set`() {
        val bundle = createReadOnlyBundle()

        assertTrue(SynchronizationBundles.bundles.isEmpty())

        SynchronizationBundles.register(bundle)

        assertEquals(bundle, SynchronizationBundles.bundles.single())
    }

    @Test
    fun `register does not add or replace an existing Bundle if we add one with duplicate key`() {
        val key = CHALLENGE_PROGRESS
        val bundle = createReadOnlyBundle(key = key)

        assertTrue(SynchronizationBundles.bundles.isEmpty())

        SynchronizationBundles.register(bundle)
        SynchronizationBundles.register(createReadOnlyBundle(key = key))

        assertEquals(bundle, SynchronizationBundles.bundles.single())
    }

    /*
    bundleForSynchronizableItem
     */
    @Test
    fun `bundleForSynchronizableItem returns single SynchronizationItemBundle that can handle synchronizableItem`() {
        val expectedSynchronizableItemBundle = synchronizableItemBundle(
            synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = PROFILES),
            dataStore = testItemDatastore(canHandle = true)
        )
        SynchronizationBundles.register(expectedSynchronizableItemBundle)

        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = ACCOUNT),
                dataStore = testItemDatastore(canHandle = false)
            )
        )

        assertEquals(
            expectedSynchronizableItemBundle,
            SynchronizationBundles.bundleForSynchronizableItem(synchronizableItem())
        )
    }

    @Test
    fun `bundleForSynchronizableItem returns null if no SynchronizationItemBundle is registered for SynchronizableItem and FailEarly is NoOp`() {
        try {
            overrideDelegateWith(NoopTestDelegate)

            assertNull(SynchronizationBundles.bundleForSynchronizableItem(synchronizableItem()))
        } finally {
            overrideDelegateWith(TestDelegate)
        }
    }

    @Test(expected = AssertionError::class)
    fun `bundleForSynchronizableItem throws exception if no SynchronizationItemBundle is registered and FailEarly is active`() {
        SynchronizationBundles.bundleForSynchronizableItem(synchronizableItem())
    }

    @Test(expected = AssertionError::class)
    fun `bundleForSynchronizableItem throws exception if no SynchronizationItemBundle can handle SynchronizableItem and FailEarly is active`() {
        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = PROFILES),
                dataStore = testItemDatastore(canHandle = false)
            )
        )

        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = ACCOUNT),
                dataStore = testItemDatastore(canHandle = false)
            )
        )

        SynchronizationBundles.bundleForSynchronizableItem(synchronizableItem())
    }

    @Test(expected = AssertionError::class)
    fun `bundleForSynchronizableItem throws exception if multiple SynchronizationItemBundle can handle SynchronizableItem and FailEarly is active`() {
        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = PROFILES),
                dataStore = testItemDatastore(canHandle = true)
            )
        )

        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = ACCOUNT),
                dataStore = testItemDatastore(canHandle = true)
            )
        )

        SynchronizationBundles.bundleForSynchronizableItem(synchronizableItem())
    }

    /*
    bundleForSynchronizatorEntity
     */

    @Test
    fun `bundleForSynchronizatorEntity returns single SynchronizationItemBundle with key equal to SynchronizatorEntity`() {
        val key = PROFILES
        val expectedSynchronizableItemBundle = synchronizableItemBundle(
            synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = key)
        )
        SynchronizationBundles.register(expectedSynchronizableItemBundle)

        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = ACCOUNT)
            )
        )

        assertEquals(
            expectedSynchronizableItemBundle,
            SynchronizationBundles.bundleForTrackingEntity(
                synchronizableTrackingEntity(
                    bundleKey = key
                )
            )
        )
    }

    @Test
    fun `bundleForSynchronizatorEntity returns null if no SynchronizationItemBundle is registered for SynchronizableItem and FailEarly is NoOp`() {
        try {
            overrideDelegateWith(NoopTestDelegate)

            assertNull(
                SynchronizationBundles.bundleForTrackingEntity(
                    synchronizableTrackingEntity()
                )
            )
        } finally {
            overrideDelegateWith(TestDelegate)
        }
    }

    @Test(expected = AssertionError::class)
    fun `bundleForSynchronizatorEntity throws exception if no SynchronizationItemBundle is registered and FailEarly is active`() {
        SynchronizationBundles.bundleForTrackingEntity(synchronizableTrackingEntity())
    }

    @Test(expected = AssertionError::class)
    fun `bundleForSynchronizatorEntity throws exception if no SynchronizationItemBundle has key equal to SynchronizatorEntity and FailEarly is active`() {
        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = PROFILES)
            )
        )

        SynchronizationBundles.register(
            synchronizableItemBundle(
                synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = ACCOUNT)
            )
        )

        SynchronizationBundles.bundleForTrackingEntity(synchronizableTrackingEntity())
    }

    /*
    Utils
     */

    private fun createReadOnlyBundle(key: SynchronizableKey = PROFILE_SMILES): Bundle {
        return SynchronizableReadOnlyBundle(
            api = mock(),
            dataStore = mock(),
            synchronizeAccountKeyBuilder = object : SynchronizeAccountKeyBuilder(key) {
                override fun version(): Int = 0
            }
        )
    }
}
