/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.utils

import com.google.common.base.Optional
import com.jakewharton.rxrelay2.PublishRelay
import com.kolibree.android.annotation.VisibleForApp
import dagger.Binds
import dagger.Module
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.PriorityQueue
import java.util.Queue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.threeten.bp.Duration
import timber.log.Timber

/**
 * All available priorities of an item
 */
@VisibleForApp
enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT;
}

/**
 * To use the queue the item in the queue need to implement this interface
 * and define the given priority. Multiple items can have the same priority
 * in that case the order will not be guarantee
 */
@VisibleForApp
interface KLItem {
    val priority: Priority
}

@VisibleForApp
interface KLQueue : KLQueueInteraction, KLQueueSubmitter

@VisibleForApp
interface KLQueueSubmitter {
    /**
     * If the item is already present in the queue it won't be added
     */
    fun submit(item: KLItem)
}

@VisibleForApp
interface KLQueueInteraction {
    /**
     * This stream will emit the current item that should be consumed.
     *
     * If there is no item to consume then it emits Optional.absent()
     *
     * When an item is emitted (except absent() it will stay on the stream (even if we subscribe again)
     * until someone consumes it.
     *
     * Once an item is consumed a delay will be apply before a new item is emit and it can be absent too
     *
     * Item that is not yet consumed can be removed from the queue without being ever emitted by this
     * stream by calling [consume].
     */
    fun stream(): Observable<Optional<KLItem>>

    /**
     * Mark an item as consumed. Consumed item is removed from the queue.
     * If the consumed item is currently emitted by the stream, it will free up space for another item.
     * If the consumed item was not emitted yet by the stream it won't ever be.
     */
    fun consume(item: KLItem)

    /**
     * Checks if queue contains given [item]
     */
    fun contains(item: KLItem): Boolean
}

/**
 * NOTE: This class can be accessed from multiple threads. Make sure it stays thread-safe.
 */
internal class KLQueueImpl constructor(
    private val delayScheduler: Scheduler,
    delayAfterConsumption: Duration
) : KLQueue {

    private val publishRelay = PublishRelay.create<Unit>()

    private val queue: Queue<KLItem> =
        PriorityQueue<KLItem>(1, compareByDescending(KLItem::priority))

    private val waitingForConsumption = AtomicBoolean(false)
    private val currentItem = AtomicReference<KLItem?>(null)

    private val delayAfterConsumptionMillis = delayAfterConsumption.toMillis()

    override fun stream(): Observable<Optional<KLItem>> =
        publishRelay
            .delay(delayAfterConsumptionMillis, TimeUnit.MILLISECONDS, delayScheduler)
            .map {
                synchronized(queue) {
                    Optional.fromNullable(queue.peek())
                }
            }
            .startWith(synchronized(queue) { Optional.fromNullable(queue.peek()) })
            .doOnNext {
                if (it.isPresent) {
                    currentItem.set(it.get())
                    waitingForConsumption.set(true)
                }
            }
            .distinctUntilChanged()
            .doOnNext { item ->
                Timber.i("(${this@KLQueueImpl}) item streamed: $item")
            }

    override fun consume(item: KLItem) {
        var shouldNotify = false
        synchronized(queue) {
            val removed = queue.remove(item)
            if (item == currentItem.get()) {
                shouldNotify = true
                waitingForConsumption.set(false)
                currentItem.set(null)
                Timber.i("(${this@KLQueueImpl}) $item consumed and notified : $removed")
            } else {
                // Do nothing if we remove an item which is not currently waiting to be consumed
                Timber.i("(${this@KLQueueImpl}) $item consumed but without notification : $removed")
            }
        }
        if (shouldNotify) {
            publishRelay.accept(Unit)
        }
    }

    override fun submit(item: KLItem) {
        var shouldNotify = false
        synchronized(queue) {
            if (queue.contains(item)) {
                Timber.i("(${this@KLQueueImpl}) $item already exist in the queue, so it won't be added")
            } else {
                Timber.i("(${this@KLQueueImpl}) $item submitted")
                queue.add(item)
                shouldNotify = waitingForConsumption.get().not()
            }
        }
        if (shouldNotify) {
            publishRelay.accept(Unit)
        }
    }

    override fun contains(item: KLItem): Boolean {
        return queue.contains(item)
    }
}

/**
 * This factory should be use to create new instance of a queue
 *
 * Example of usage :
 * <pre>
 *   @Module(includes = [KLQueueModule::class])
 *   object MyModule {
 *
 *      @AppScope
 *      @Provides
 *      fun providesQueue(scheduler: Scheduler): KLQueue {
 *          return KLQueueFactory.creates(scheduler)
 *      }
 *   }
 * </pre>
 */
@VisibleForApp
object KLQueueFactory {
    fun creates(
        delayScheduler: Scheduler,
        delayAfterConsumption: Duration = Duration.ofMillis(DELAY_AFTER_CONSUMPTION_MILLISECONDS)
    ): KLQueue = KLQueueImpl(delayScheduler, delayAfterConsumption)
}

/**
 * This module binds a KLQueue to a given interaction and submitter.
 * To create a queue the [KLQueueFactory] should be use and a scope need to be defined to
 * avoid creating a new queue each time you will inject it.
 */
@Module
abstract class KLQueueModule {

    @Binds
    internal abstract fun bindsKLQueueInteraction(queue: KLQueue): KLQueueInteraction

    @Binds
    internal abstract fun bindsKLQueueSubmitter(queue: KLQueue): KLQueueSubmitter
}

const val DELAY_AFTER_CONSUMPTION_MILLISECONDS = 1000L
