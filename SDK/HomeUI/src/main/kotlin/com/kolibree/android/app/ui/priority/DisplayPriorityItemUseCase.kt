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
import timber.log.Timber

@VisibleForApp
interface DisplayPriorityItemUseCase<DISPLAY_ITEM : KLItem> {

    /**
     * Submits [item] to the queue for display and then waits until it is ready to be displayed.
     * @return [Completable] that will complete when submitted [item] can be displayed on screen.
     */
    fun submitAndWaitFor(item: DISPLAY_ITEM): Completable

    /**
     *  Marks [item] as displayed allowing next items to be displayed
     */
    fun markAsDisplayed(item: DISPLAY_ITEM)
}

@VisibleForApp
object DisplayPriorityUseCaseFactory {
    fun <DISPLAY_ITEM : KLItem> create(
        queue: KLQueue
    ): DisplayPriorityItemUseCase<DISPLAY_ITEM> {
        return DisplayPriorityItemUseCaseImpl(queue)
    }
}

internal class DisplayPriorityItemUseCaseImpl<DISPLAY_ITEM : KLItem> @Inject constructor(
    private val priorityQueue: KLQueue
) : DisplayPriorityItemUseCase<DISPLAY_ITEM> {

    override fun submitAndWaitFor(item: DISPLAY_ITEM): Completable {
        return Completable
            .fromCallable { priorityQueue.submit(item) }
            .doOnSubscribe { Timber.d("Waiting for: $item") }
            .andThen(Observable.defer { priorityQueue.stream() })
            .makeSureQueueContainsItem(item)
            .filterItem(item)
            .take(1)
            .ignoreElements()
    }

    override fun markAsDisplayed(item: DISPLAY_ITEM) {
        priorityQueue.consume(item)
        Timber.d("Mark as displayed: $item")
    }

    /**
     * Checks if [item] that we are waiting for is present in [priorityQueue].
     * If not stops stream with [IllegalStateException].
     */
    private fun <T> Observable<T>.makeSureQueueContainsItem(item: DISPLAY_ITEM): Observable<T> {
        return map {
            if (priorityQueue.contains(item)) it
            else throw IllegalStateException("Item that we are waiting for is not present in the queue!")
        }
    }

    private fun Observable<Optional<KLItem>>.filterItem(item: KLItem): Observable<Optional<KLItem>> {
        return filter { queueItem -> queueItem.isPresent && queueItem.get() == item }
    }
}
