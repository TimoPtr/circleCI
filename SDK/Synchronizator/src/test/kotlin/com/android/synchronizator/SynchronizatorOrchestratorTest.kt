package com.android.synchronizator

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.SynchronizatorOrchestrator
import com.kolibree.android.synchronizator.operations.CreateOrEditOperation
import com.kolibree.android.synchronizator.operations.DeleteOperation
import com.kolibree.android.synchronizator.operations.SynchronizeOperation
import com.kolibree.android.synchronizator.operations.UpdateOperation
import com.kolibree.android.synchronizator.operations.utils.OperationProvider
import com.kolibree.android.synchronizator.operations.utils.mockCreateOrEdit
import com.kolibree.android.synchronizator.operations.utils.mockSync
import com.kolibree.android.synchronizator.operations.utils.mockUpdate
import com.kolibree.android.synchronizator.operations.utils.provider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.subjects.CompletableSubject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class SynchronizatorOrchestratorTest : BaseUnitTest() {

    private val synchronizeOperationProvider: OperationProvider<SynchronizeOperation> = provider()
    private val createOrEditOperationProvider: OperationProvider<CreateOrEditOperation> = provider()
    private val deleteOperationProvider: OperationProvider<DeleteOperation> = provider()
    private val updateOperationProvider: OperationProvider<UpdateOperation> = provider()
    private val queueOperationExecutor: QueueOperationExecutor = mock()

    private val synchronizator = SynchronizatorOrchestrator(
        synchronizeOperationProvider = synchronizeOperationProvider.provider(),
        createOrEditOperationProvider = createOrEditOperationProvider.provider(),
        updateOperationProvider = updateOperationProvider.provider(),
        deleteOperationProvider = deleteOperationProvider.provider(),
        queueOperationExecutor = queueOperationExecutor
    )

    override fun setup() {
        super.setup()

        SynchronizationBundles.clear()
    }

    override fun tearDown() {
        super.tearDown()

        SynchronizationBundles.clear()
    }

    @Test
    fun `sandBy() sets canSynchronize to false`() {
        synchronizator.canSynchronize.set(true)

        synchronizator.standBy()

        assertFalse(synchronizator.canSynchronize.get())
    }

    @Test
    fun `resume() sets canSynchronize to true`() {
        synchronizator.canSynchronize.set(false)

        synchronizator.resume()

        assertTrue(synchronizator.canSynchronize.get())
    }

    @Test
    fun `synchronize after standBy() does nothing`() {
        synchronizator.canSynchronize.set(true)

        synchronizator.standBy()
        synchronizator.synchronize()

        verify(synchronizeOperationProvider.provider(), never()).get()
    }

    @Test
    fun synchronizeCompletable_without_pause_invokes_internalSynchronize() {
        synchronizator.canSynchronize.set(true)

        val operationSubject = CompletableSubject.create()
        synchronizeOperationProvider.mockSync { operationSubject }

        val observer = synchronizator.synchronizeCompletable().test()
            .assertNotComplete()

        assertTrue(operationSubject.hasObservers())
        operationSubject.onComplete()

        observer.assertComplete()
    }

    @Test
    fun synchronize_without_pause_invokes_internalSynchronize() {
        synchronizator.canSynchronize.set(true)

        synchronizeOperationProvider.mockSync()

        synchronizator.synchronize()

        verify(synchronizeOperationProvider.provider()).get()

        verify(synchronizeOperationProvider.operations.single()).run()
    }

    /*
    create
     */
    @Test
    fun `create runs createOrEditOperation`() {
        val item = synchronizableItem()

        createOrEditOperationProvider.mockCreateOrEdit()

        synchronizator.create(item).test()

        verify(createOrEditOperationProvider.provider()).get()

        verify(createOrEditOperationProvider.operations.single()).run(item)
    }

    /*
    update
     */
    @Test
    fun `update runs updateOperation`() {
        val item = synchronizableItem()

        updateOperationProvider.mockUpdate()

        synchronizator.update(item).test()

        verify(updateOperationProvider.provider()).get()

        verify(updateOperationProvider.operations.single()).run(item)
    }

    /*
    cancelAll
     */
    @Test
    fun `cancelAll invokes queueOperationExecutor cancelAll`() {
        synchronizator.cancelAll()

        verify(queueOperationExecutor).cancelOperations()
    }
}
