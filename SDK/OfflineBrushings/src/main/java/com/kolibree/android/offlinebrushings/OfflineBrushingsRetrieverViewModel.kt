/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kolibree.android.SHARED_MODE_PROFILE_ID
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.sdk.connection.state.ActiveConnectionUseCase
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

/**
 * ViewModel that will extract offline brushings every time a connection becomes Active
 */
@Keep
@Deprecated("See com.kolibree.android.offlinebrushings.retriever.OfflineExtractionProgressPublisher")
class OfflineBrushingsRetrieverViewModel(
    private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase,
    private val activeConnectionsStateObservable: ActiveConnectionUseCase,
    private val currentProfileProvider: CurrentProfileProvider
) : ViewModel() {

    @VisibleForTesting
    internal val disposables = CompositeDisposable()

    @VisibleForTesting
    internal var currentProfileId: Long? = null

    init {
        subscribeToCurrentProfile()
    }

    override fun onCleared() {
        super.onCleared()

        disposables.dispose()
    }

    private fun subscribeToCurrentProfile() {
        disposables += currentProfileProvider.currentProfileFlowable()
            .subscribe(
                { currentProfileId = it.id },
                Timber::e
            )
    }

    /**
     * @return [Flowable] that will emit [OfflineBrushingsRetrieverViewState] whenever the active
     * profile has synchronized new offline brushings
     */
    @get:JvmName("viewStateObservable")
    val viewStateObservable: Flowable<OfflineBrushingsRetrieverViewState>
        get() = activeConnectionsStateObservable.onConnectionsUpdatedStream()
            .subscribeOn(Schedulers.io())
            .flatMapSingle {
                extractOfflineBrushingsUseCase.extractOfflineBrushings().lastOrError()
                    .map {
                        it.brushingsSynced.filterByProfileId(currentProfileId)
                    }
            }
            .filter(List<BrushingSyncedResult>::isNotEmpty)
            .map { OfflineBrushingsRetrieverViewState.withRecordsRetrieved(it.size) }
            .startWith(initialViewState())

    internal fun initialViewState(): OfflineBrushingsRetrieverViewState =
        OfflineBrushingsRetrieverViewState.empty()

    class Factory @Inject constructor(
        private val extractOfflineBrushingsUseCase: ExtractOfflineBrushingsUseCase,
        private val currentProfileProvider: CurrentProfileProvider,
        private val activeConnectionsStateObservable: ActiveConnectionUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OfflineBrushingsRetrieverViewModel(
                extractOfflineBrushingsUseCase,
                activeConnectionsStateObservable,
                currentProfileProvider
            ) as T
    }
}

private fun List<BrushingSyncedResult>.filterByProfileId(profileId: Long?): List<BrushingSyncedResult> {
    if (profileId == null) return emptyList()

    return filter { it.profileId == SHARED_MODE_PROFILE_ID || it.profileId == profileId }
}
