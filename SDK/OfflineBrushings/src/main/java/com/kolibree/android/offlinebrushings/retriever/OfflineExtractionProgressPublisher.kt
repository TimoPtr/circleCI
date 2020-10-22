/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.retriever

import android.os.Handler
import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.app.dagger.AppScope
import com.kolibree.android.offlinebrushings.ExtractionProgress
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 * Publisher to observe [ExtractionProgress] events and know when they occurred
 *
 * Observers can choose to flag a [TimestampedExtractionProgress] as consumed to avoid emissions of
 * the same instance in the future
 *
 * For example, we open a Checkup screen after extraction completes. If we don't invoke [consume]
 * with the [TimestampedExtractionProgress] that signals offline extraction completion, when we
 * navigate back to Home screen and resubscribe to [OfflineExtractionProgressPublisher], we will
 * receive the same [TimestampedExtractionProgress], entering an infinite loop of Home - Checkup
 * redirections
 */
@Keep
interface OfflineExtractionProgressPublisher {
    /**
     * Scheduler: this stream emits by default on MainThread scheduler
     *
     * The returned [Observable] will not emit onError or onComplete events
     *
     * @return [Observable]<[TimestampedExtractionProgress]> that will emit the latest
     * [TimestampedExtractionProgress], if any, as well as any [TimestampedExtractionProgress] that
     * happens while the stream is active
     */
    fun stream(): Observable<TimestampedExtractionProgress>

    /**
     * Mark [item] as consumed.
     *
     * if, at the time of subscription, [item] is the latest [TimestampedExtractionProgress] held by
     * the internal queue (the event that will be served to a new observer of [stream]), it will be
     * replaced by a [TimestampedExtractionProgress] with empty [ExtractionProgress].
     *
     * If [item] is not the latest [TimestampedExtractionProgress] in the queue, the returned
     * [Completable] behaves as a no-op
     *
     * @return [Completable], non-blocking
     */
    fun consume(item: TimestampedExtractionProgress): Completable
}

/**
 * Consumer of [ExtractionProgress] events
 */
internal interface OfflineExtractionProgressConsumer {
    /**
     * Non-blocking operation that accepts [extractionProgress]
     */
    fun accept(extractionProgress: ExtractionProgress)
}

@AppScope
internal class OfflineRetrieveStatusPublisherImpl
@VisibleForTesting constructor(private val mainHandler: Handler) :
    OfflineExtractionProgressPublisher,
    OfflineExtractionProgressConsumer {

    @Inject
    constructor() : this(Handler(Looper.getMainLooper()))

    private val progressRelay = BehaviorRelay.create<TimestampedExtractionProgress>()

    override fun stream(): Observable<TimestampedExtractionProgress> = progressRelay

    override fun accept(extractionProgress: ExtractionProgress) {
        mainHandler.post {
            progressRelay.accept(
                TimestampedExtractionProgress.fromExtractionProgress(extractionProgress)
            )
        }
    }

    override fun consume(item: TimestampedExtractionProgress): Completable =
        Completable.fromAction {
            if (progressRelay.value == item && !item.isEmpty()) {
                accept(ExtractionProgress.empty())
            }
        }
}

private fun TimestampedExtractionProgress.isEmpty(): Boolean {
    return extractionProgress == ExtractionProgress.empty()
}
