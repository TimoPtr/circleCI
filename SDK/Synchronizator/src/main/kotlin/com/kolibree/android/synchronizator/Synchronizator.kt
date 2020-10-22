package com.kolibree.android.synchronizator

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.operations.CreateOrEditOperation
import com.kolibree.android.synchronizator.operations.DeleteOperation
import com.kolibree.android.synchronizator.operations.SynchronizeOperation
import com.kolibree.android.synchronizator.operations.UpdateOperation
import io.reactivex.Completable
import io.reactivex.Single
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass
import org.threeten.bp.Duration
import timber.log.Timber

@Keep
interface Synchronizator {

    /**
     * Schedules a Synchronization that will run at some point in the future
     *
     * If any part of the code invoked [standBy] and no one invoked [resume], the returned
     * Completable will complete without scheduling a Synchronization
     *
     * It DOES NOT wait until synchronization is completed
     */
    @Deprecated(
        message = "Favor synchronizeCompletable. Internally, it's invoked in blockingAwait",
        replaceWith = ReplaceWith(("synchronizeCompletable"))
    )
    fun synchronize()

    /**
     * Schedules a Synchronization that will run at some point in the future
     *
     * If any part of the code invoked [standBy] and no one invoked [resume], the returned
     * Completable will complete without scheduling a Synchronization
     *
     * @param initialDelay minimum delay after which synchronization can be started
     *
     * @return Completable that will complete as soon as a Synchronize operation is scheduled
     *
     * It DOES NOT wait until synchronization is completed
     */
    fun synchronizeCompletable(initialDelay: Duration = Duration.ZERO): Completable

    fun delaySynchronizeCompletable() = synchronizeCompletable(DEFAULT_SYNC_DELAY)

    /**
     * Handles creation of [synchronizableItem]. It deals with local persistence and
     * schedules the remote upload to the server, which will run at some point in the future.
     *
     * If no [SynchronizableItemBundle] is registered for [synchronizableItem], the Single will
     * emit an error
     *
     * @return Single that will emit the [SynchronizableItem] resulting from creating
     * [synchronizableItem]
     *
     * It DOES NOT wait until [synchronizableItem] is uploaded remotely
     */
    fun create(synchronizableItem: SynchronizableItem): Single<SynchronizableItem>

    /**
     * Handles edition of [synchronizableItem]. It deals with local persistence and
     * schedules the remote upload to the server, which will run at some point in the future.
     *
     * If no [SynchronizableItemBundle] is registered for [synchronizableItem], the Single will
     * emit an error
     *
     * If [synchronizableItem] uuid is null, the returned [Single] will emit an error and no local
     * changes will take place.
     *
     * @return Single that will emit the [SynchronizableItem] resulting from editing
     * [synchronizableItem]
     *
     * It DOES NOT wait until [synchronizableItem] is uploaded remotely
     */
    fun update(synchronizableItem: SynchronizableItem): Single<SynchronizableItem>

    fun standBy()
    fun resume()

    /**
     * Cancels all running or pending operations
     *
     * Blocking operation
     */
    fun cancelAll()

    companion object {
        private val DEFAULT_SYNC_DELAY = Duration.ofSeconds(2)
    }
}

@AppScope
internal class SynchronizatorOrchestrator @Inject constructor(
    private val synchronizeOperationProvider: Provider<SynchronizeOperation>,
    private val createOrEditOperationProvider: Provider<CreateOrEditOperation>,
    private val updateOperationProvider: Provider<UpdateOperation>,
    private val deleteOperationProvider: Provider<DeleteOperation>,
    private val queueOperationExecutor: QueueOperationExecutor
) : Synchronizator {

    @VisibleForTesting
    val canSynchronize = AtomicBoolean(true)

    override fun standBy() {
        canSynchronize.set(false)
    }

    override fun resume() {
        canSynchronize.set(true)
    }

    override fun synchronize() {
        return synchronizeCompletable().blockingAwait()
    }

    override fun synchronizeCompletable(initialDelay: Duration): Completable {
        return Single.fromCallable { canSynchronize.get() }
            .flatMapCompletable { canSynchronize ->
                if (canSynchronize) {
                    synchronizeOperationProvider.get().run(initialDelay)
                } else {
                    Completable.complete()
                        .doOnSubscribe { Timber.w("Not synchronizing, canSynchronize is false") }
                }
            }
    }

    override fun create(synchronizableItem: SynchronizableItem): Single<SynchronizableItem> {
        return createOrEditOperationProvider.get().run(synchronizableItem)
    }

    override fun update(synchronizableItem: SynchronizableItem): Single<SynchronizableItem> {
        return updateOperationProvider.get().run(synchronizableItem)
    }

    override fun cancelAll() = queueOperationExecutor.cancelOperations()
}

internal fun <T : Any> T.tag() = syncTagFor(this::class)
internal fun syncTagFor(clazz: KClass<*>): String = "SYNC|${clazz.java.simpleName}" // SYNC|
