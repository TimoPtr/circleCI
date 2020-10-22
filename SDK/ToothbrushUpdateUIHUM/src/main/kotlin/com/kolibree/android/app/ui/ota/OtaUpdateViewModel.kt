/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.ota

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.Error
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.sdk.core.ServiceDisconnected
import com.kolibree.android.sdk.core.ServiceProvider
import com.kolibree.databinding.livedata.LiveDataTransformations
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.distinctUntilChanged
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

internal class OtaUpdateViewModel(
    initialViewState: OtaUpdateViewState?,
    private val serviceProvider: ServiceProvider
) : BaseViewModel<OtaUpdateViewState, OtaUpdateActions>(
    initialViewState ?: OtaUpdateViewState.initial()
), OtaUpdateSharedViewModel {

    val snackbarConfiguration = LiveDataTransformations.twoWayMap(viewStateLiveData,
        { viewState -> viewState?.snackbarConfiguration },
        { configuration -> configuration?.let { updateViewState { copy(snackbarConfiguration = configuration) } } })

    val progressVisible: LiveData<Boolean> = map(viewStateLiveData) { viewState ->
        viewState?.progressVisible ?: false
    }.distinctUntilChanged()

    override fun showError(error: Error) {
        updateViewState {
            copy(snackbarConfiguration = SnackbarConfiguration(isShown = true, error = error))
        }
    }

    override fun hideError() {
        updateViewState {
            withSnackbarDismissed()
        }
    }

    override fun showProgress(show: Boolean) {
        updateViewState {
            copy(progressVisible = show)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy { keepServiceAlive() }
    }

    /*
    We need to keep the service alive during the whole OTA
     */
    private fun keepServiceAlive(): Disposable = serviceProvider.connectStream()
        .subscribe({
            if (it is ServiceDisconnected) {
                Timber.w("Service disconnected")
            }
        }, Timber::e)

    class Factory @Inject constructor(
        private val serviceProvider: ServiceProvider
    ) : BaseViewModel.Factory<OtaUpdateViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OtaUpdateViewModel(
                viewState,
                serviceProvider
            ) as T
    }
}
