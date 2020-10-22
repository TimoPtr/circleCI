/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.retriever

import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.offlinebrushings.OfflineBrushingsRetrieverUseCase
import com.kolibree.android.utils.lifecycle.ApplicationLifecycleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * Class that will start Offline Brushings retrieval when application comes to foreground, and stop
 * retrieval once application is moved to background
 */
internal class OfflineBrushingRetrieverMonitor
@Inject constructor(
    private val offlineBrushingsRetrieverUseCase: OfflineBrushingsRetrieverUseCase,
    private val consumer: OfflineExtractionProgressConsumer
) : ApplicationLifecycleObserver {
    private var disposable: Disposable? = null

    override fun onApplicationStarted() {
        disposable = offlineBrushingsRetrieverUseCase.stream()
            .subscribeOn(Schedulers.io())
            .doOnNext(consumer::accept)
            .subscribe(
                { },
                Timber::e
            )
    }

    override fun onApplicationStopped() = disposable.forceDispose()
}
