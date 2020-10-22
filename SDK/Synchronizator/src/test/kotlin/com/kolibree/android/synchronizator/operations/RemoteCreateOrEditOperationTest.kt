/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.android.synchronizator.remoteSynchronizableItem
import com.android.synchronizator.synchronizableItem
import com.android.synchronizator.synchronizableItemBundle
import com.android.synchronizator.synchronizableItemWrapper
import com.android.synchronizator.synchronizeAccountKeyBuilder
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus.PENDING
import com.kolibree.android.synchronizator.network.CreateOrEditHttpErrorInterceptor
import com.kolibree.android.synchronizator.operations.usecases.ConflictResolutionUseCase
import com.kolibree.android.test.TestForcedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.io.IOException
import junit.framework.TestCase.assertNotSame
import org.junit.Test

internal class RemoteCreateOrEditOperationTest : BaseUnitTest() {
    private val errorInterceptor: CreateOrEditHttpErrorInterceptor = mock()
    private val conflictResolutionUseCase: ConflictResolutionUseCase = mock()
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase = mock()
    val wrapper = synchronizableItemWrapper()

    private val operation = spy(
        RemoteCreateOrEditQueueOperation(
            wrapper,
            errorInterceptor,
            conflictResolutionUseCase,
            updateUploadStatusUseCase
        )
    )

    override fun setup() {
        super.setup()

        SynchronizationBundles.clear()
    }

    override fun tearDown() {
        super.tearDown()

        SynchronizationBundles.clear()
    }

    /*
    onOperationNotRun
     */

    @Test(expected = AssertionError::class)
    fun `onOperationNotRun emits error if there's no bundle registered`() {
        operation.onOperationNotRun()
    }

    @Test(expected = AssertionError::class)
    fun `onOperationNotRun emits error if there's no bundle registered for SynchronizableItem`() {
        val bundleKey = SynchronizableKey.PROFILES

        assertNotSame(bundleKey, wrapper.bundleKey)

        prepareBundle(key = bundleKey)

        operation.onOperationNotRun()
    }

    @Test
    fun `onOperationNotRun flags Tracking Entity as PENDING`() {
        val bundle = prepareBundle()

        operation.onOperationNotRun()

        verify(updateUploadStatusUseCase).update(wrapper, bundle, PENDING)
    }

    /*
    run
     */

    @Test(expected = AssertionError::class)
    fun `run emits error if there's no bundle registered`() {
        operation.run()
    }

    @Test(expected = AssertionError::class)
    fun `run emits error if there's no bundle registered for SynchronizableItem`() {
        val bundleKey = SynchronizableKey.PROFILES

        assertNotSame(bundleKey, wrapper.bundleKey)

        prepareBundle(key = bundleKey)

        operation.run()
    }

    @Test(expected = TestForcedException::class)
    fun `run throws exception if bundle api throws exception and errorInterceptor rethrows it`() {
        val bundle = prepareBundle()
        val exception = TestForcedException()

        whenever(bundle.api.createOrEdit(wrapper.synchronizableItem))
            .thenAnswer { throw exception }

        whenever(errorInterceptor.intercept(exception, wrapper, bundle))
            .thenAnswer { throw it.getArgument(0) }

        operation.run()
    }

    @Test
    fun `run captures exception if errorInterceptor doesn't rethrow it`() {
        val bundle = prepareBundle()

        val expectedException = IOException("")
        whenever(bundle.api.createOrEdit(wrapper.synchronizableItem))
            .thenAnswer { throw expectedException }

        whenever(errorInterceptor.intercept(TestForcedException(), wrapper, bundle))
            .thenAnswer {
                // no-op
            }

        operation.run()

        verify(errorInterceptor).intercept(expectedException, wrapper, bundle)
    }

    @Test
    fun `run invokes conflictResolutionUseCase with ConflictResolution`() {
        val bundle = prepareBundle()

        val remoteSynchronizable = remoteSynchronizableItem()
        whenever(bundle.api.createOrEdit(wrapper.synchronizableItem)).thenReturn(
            remoteSynchronizable
        )

        val expectedConflictResolution = ConflictResolution(
            localSynchronizable = wrapper.synchronizableItem,
            remoteSynchronizable = remoteSynchronizable,
            resolvedSynchronizable = synchronizableItem()
        )
        whenever(bundle.conflictStrategy.resolve(wrapper.synchronizableItem, remoteSynchronizable))
            .thenReturn(
                expectedConflictResolution
            )

        operation.run()

        verify(conflictResolutionUseCase).resolve(expectedConflictResolution, bundle)
    }

    /*
    Utils
     */

    private fun prepareBundle(key: SynchronizableKey = wrapper.bundleKey): SynchronizableItemBundle {
        val bundle = synchronizableItemBundle(
            synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder(key = key)
        )

        SynchronizationBundles.register(bundle)

        return bundle
    }
}
