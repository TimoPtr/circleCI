/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.priority

import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.KLQueue
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject
import kotlin.reflect.KClass
import timber.log.Timber

@VisibleForApp
interface AsyncDisplayItemUseCase<DISPLAY_ITEM : KLItem> {

    /**
     * Submits [item] to the queue for display and completes immediately.
     * @return [Completable] that will complete when provided [item] is submitted to the queue.
     */
    fun submit(item: DISPLAY_ITEM): Completable

    /**
     * Waits until an [item] can be displayed on screen.
     * If the item was not submitted via [submit], stream will wait until someone else submits
     * the item and is ready to be displayed.
     * @return [Completable] that will complete when [item] is ready to be displayed.
     */
    fun waitFor(item: DISPLAY_ITEM): Completable

    /**
     * Waits until an item of [itemType] can be displayed on screen.
     * If the item was not submitted via [submit], stream will wait until someone else submits
     * the item and is ready to be displayed.
     * @return [Observable] that will emit every item of [itemType] when it's ready to be displayed.
     */
    fun <T : DISPLAY_ITEM> listenFor(itemType: KClass<T>): Observable<T>

    /**
     *  Marks [item] as displayed allowing next items to be displayed
     */
    fun markAsDisplayed(item: DISPLAY_ITEM)
}

@VisibleForApp
object AsyncDisplayItemUseCaseFactory {
    fun <DISPLAY_ITEM : KLItem> create(
        queue: KLQueue
    ): AsyncDisplayItemUseCase<DISPLAY_ITEM> {
        return AsyncDisplayItemUseCaseImpl(queue)
    }
}

internal class AsyncDisplayItemUseCaseImpl<DISPLAY_ITEM : KLItem> @Inject constructor(
    private val priorityQueue: KLQueue
) : AsyncDisplayItemUseCase<DISPLAY_ITEM> {

    override fun submit(item: DISPLAY_ITEM): Completable =
        Completable.fromCallable { priorityQueue.submit(item) }

    override fun waitFor(item: DISPLAY_ITEM): Completable =
        priorityQueue.stream()
            .doOnSubscribe { Timber.d("Waiting for: $item") }
            .filter { queueItem -> queueItem.isPresent && queueItem.get() == item }
            .take(1)
            .ignoreElements()

    override fun <T : DISPLAY_ITEM> listenFor(itemType: KClass<T>): Observable<T> =
        priorityQueue.stream()
            .doOnSubscribe { Timber.d("Listening for: $itemType") }
            .filterItemClass(itemType)
            .map { it.get() as T }

    override fun markAsDisplayed(item: DISPLAY_ITEM) {
        priorityQueue.consume(item)
        Timber.d("Mark as displayed: $item")
    }

    private fun Observable<Optional<KLItem>>.filterItemClass(
        itemClass: KClass<out DISPLAY_ITEM>
    ): Observable<Optional<KLItem>> {
        return filter { queueItem -> queueItem.isPresent && queueItem.get()::class == itemClass }
    }
}
