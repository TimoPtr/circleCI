/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.sdk.core.detectLeaks
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import javax.inject.Inject

internal interface OfflineBrushingsRetrieverUseCase {
    fun stream(): Flowable<ExtractionProgress>
}

/**
 * This use case is used on top of [ExtractOfflineBrushingsUseCase] to retrieves the OfflineBrushing of
 * all the active connections
 */
internal class OfflineBrushingsRetrieverUseCaseImpl @Inject constructor(
    private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase,
    private val retrieverTrigger: OfflineBrushingsRetrieverTrigger
) : OfflineBrushingsRetrieverUseCase {

    @Volatile
    private var sharedStream: Flowable<ExtractionProgress>? = null

    override fun stream(): Flowable<ExtractionProgress> {
        var localRef: Flowable<ExtractionProgress>? = sharedStream
        if (localRef == null) {
            synchronized(this) {
                localRef = sharedStream
                if (localRef == null) {
                    localRef = createStream()

                    sharedStream = localRef
                }
            }
        }

        if (localRef == null) {
            FailEarly.fail("localRef was null")
        }

        return checkNotNull(localRef)
    }

    private fun createStream(): Flowable<ExtractionProgress> {
        return retrieverTrigger.trigger
            .flatMap {
                extractOfflineBrushingsUseCase.extractOfflineBrushings()
                    .toFlowable(BackpressureStrategy.LATEST)
            }
            .startWith(ExtractionProgress.empty())
            .doFinally { nullifyStream() }
            .share()
    }

    private fun nullifyStream() {
        synchronized(this) {
            sharedStream.detectLeaks("offlineBrushingsStream")

            sharedStream = null
        }
    }
}
