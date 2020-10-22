/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.startscreen

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
interface ActivityStartPreconditions {
    val canStart: LiveData<Boolean>
}

/**
 * Check whether or not an activity can be started
 */
@VisibleForApp
class ActivityStartPreconditionsViewModel @Inject constructor(
    initialViewState: ActivityStartPreconditionsViewState,
    private val connectionProvider: KLTBConnectionProvider,
    private val macAddress: String?
) : BaseViewModel<ActivityStartPreconditionsViewState, NoActions>(initialViewState), ActivityStartPreconditions {

    override val canStart = mapNonNull(
        viewStateLiveData,
        initialViewState.canStart
    ) { viewState ->
        viewState.canStart
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::checkPreconditions)
    }

    private fun canStartActivityStream(mac: String): Flowable<Boolean> =
        connectionProvider.existingActiveConnection(mac).flatMapPublisher {
            it.vibrator().vibratorStream.map(Boolean::not)
        }

    private fun checkPreconditions(): Disposable {
        val preconditionsChecker = macAddress?.let(::canStartActivityStream) ?: Flowable.just(true)

        return preconditionsChecker.subscribe({
            updateViewState { copy(canStart = it) }
        }, Timber::e)
    }

    internal class Factory @Inject constructor(
        private val connectionProvider: KLTBConnectionProvider,
        private val macAddress: String?
    ) : BaseViewModel.Factory<ActivityStartPreconditionsViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            ActivityStartPreconditionsViewModel(
                viewState ?: ActivityStartPreconditionsViewState.initial(),
                connectionProvider,
                macAddress
            ) as T
    }
}
