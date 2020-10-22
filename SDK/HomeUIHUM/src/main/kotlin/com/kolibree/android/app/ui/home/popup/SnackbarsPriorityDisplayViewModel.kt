/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.google.common.base.Optional
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Bluetooth
import com.kolibree.android.app.ui.home.popup.DisplayedItem.Location
import com.kolibree.android.app.ui.home.popup.KLSnackbarItem.SnackbarBluetoothItem
import com.kolibree.android.app.ui.home.popup.KLSnackbarItem.SnackbarLocationItem
import com.kolibree.android.app.ui.home.popup.snackbar.BluetoothPermissionCallback
import com.kolibree.android.app.ui.home.popup.snackbar.LocationPermissionCallback
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Disabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Enabled
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.PermissionDenied
import com.kolibree.android.app.ui.home.popup.snackbar.LocationState.Unknown
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarBluetoothViewModel
import com.kolibree.android.app.ui.home.popup.snackbar.SnackbarLocationViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.widget.snackbar.SnackbarConfiguration
import com.kolibree.android.utils.KLItem
import com.kolibree.android.utils.KLQueue
import com.kolibree.databinding.livedata.LiveDataTransformations.twoWayMap
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
interface SnackbarCallback : LocationPermissionCallback, BluetoothPermissionCallback {
    val snackbarConfiguration: MediatorLiveData<SnackbarConfiguration>

    fun onSnackBarDismissed()

    fun onSnackBarClicked()

    fun onLocationSettingsClosed()
}

@VisibleForApp
class SnackbarsPriorityDisplayViewModel(
    initialViewStateSnackbars: SnackbarsPriorityDisplayViewState?,
    private val snackbarLocationViewModel: SnackbarLocationViewModel,
    private val snackbarBluetoothViewModel: SnackbarBluetoothViewModel,
    private val priorityQueue: KLQueue,
    private val navigator: HumHomeNavigator
) : BaseViewModel<SnackbarsPriorityDisplayViewState, HomeScreenAction>(
    initialViewStateSnackbars ?: SnackbarsPriorityDisplayViewState.initial(),
    children = setOf(snackbarLocationViewModel, snackbarBluetoothViewModel)
), SnackbarCallback,
    LocationPermissionCallback by snackbarLocationViewModel,
    BluetoothPermissionCallback by snackbarBluetoothViewModel {

    override val snackbarConfiguration = twoWayMap(viewStateLiveData,
        { state -> state?.snackbarConfiguration },
        { configuration ->
            configuration?.let { updateViewState { withSnackbarShown(configuration.isShown) } }
        })

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        disposeOnDestroy(::observePriorityQueue)
        disposeOnDestroy(::startBluetoothSnackbarChecker)
        disposeOnDestroy(::startLocationSnackbarChecker)
        disposeOnDestroy(::startLocationStateChecker)
    }

    //region Streams Init

    private fun observePriorityQueue(): Disposable =
        priorityQueue.stream().subscribe(::onItemReceived, Timber::i)

    private fun startBluetoothSnackbarChecker(): Disposable =
        snackbarBluetoothViewModel.startBluetoothSnackbarChecker()
            .distinctUntilChanged()
            .subscribe({ show -> onItemChecker(show, SnackbarBluetoothItem) }, Timber::e)

    private fun startLocationSnackbarChecker(): Disposable =
        snackbarLocationViewModel.startLocationSnackbarChecker()
            .distinctUntilChanged()
            .subscribe({ show -> onItemChecker(show, SnackbarLocationItem) }, Timber::e)

    private fun startLocationStateChecker() =
        snackbarLocationViewModel.viewStateFlowable
            .map { it.locationState }
            .subscribe(::onLocationStateChanged, Timber::e)

    //endregion Streams Init

    /**
     * Called when a Item is received from the Priority Queue
     * The KLItem can be null, in this case it's our responsibility to retrieve
     * the last item displayed and hide it
     */
    private fun onItemReceived(optional: Optional<KLItem>) {
        val displayedItem = PriorityItemMapper(optional.orNull() as? KLSnackbarItem)

        if (displayedItem != null) {
            updateViewState { withDisplayedItem(displayedItem) }
        } else {
            updateViewState { withDisplayedItem() }
        }
    }

    private fun onItemChecker(showSnackbar: Boolean, item: KLSnackbarItem) {
        if (showSnackbar) {
            priorityQueue.submit(item)
        } else {
            priorityQueue.consume(item)
        }
    }

    override fun onSnackBarDismissed() {
        when (getViewState()?.displayedItem) {
            Location -> {
                snackbarLocationViewModel.onLocationDismiss()
                priorityQueue.consume(SnackbarLocationItem)
            }
            Bluetooth -> {
                snackbarBluetoothViewModel.onBluetoothDismiss()
                priorityQueue.consume(SnackbarBluetoothItem)
            }
            else -> Unit
        }
    }

    override fun onSnackBarClicked() {
        when (getViewState()?.displayedItem) {
            Location -> navigator.launchLocationPermission()
            Bluetooth -> navigator.launchBluetoothPermission()
            else -> Unit
        }
    }

    private fun onLocationStateChanged(locationState: LocationState) = when (locationState) {
        PermissionDenied -> {
            priorityQueue.consume(SnackbarLocationItem)
            updateViewState { withDisplayedItem(DisplayedItem.LocationPermissionError) }
        }
        Disabled -> {
            navigator.navigateToLocationSettings()
        }
        Enabled -> {
            priorityQueue.consume(SnackbarLocationItem)
        }
        Unknown -> {
            // no-op
        }
    }

    override fun onLocationSettingsClosed() {
        priorityQueue.consume(SnackbarLocationItem)
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val priorityQueue: KLQueue,
        private val snackbarLocationViewModel: SnackbarLocationViewModel,
        private val snackbarBluetoothViewModel: SnackbarBluetoothViewModel,
        private val navigator: HumHomeNavigator
    ) : BaseViewModel.Factory<SnackbarsPriorityDisplayViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SnackbarsPriorityDisplayViewModel(
                viewState,
                snackbarLocationViewModel,
                snackbarBluetoothViewModel,
                priorityQueue,
                navigator
            ) as T
    }
}

/**
 * Map a [KLItem].[KLSnackbarItem] to it's ViewState representation -> [DisplayedItem]
 */
private object PriorityItemMapper : (KLSnackbarItem?) -> DisplayedItem? {
    override fun invoke(snackbarItem: KLSnackbarItem?) = when (snackbarItem) {
        SnackbarLocationItem -> Location
        SnackbarBluetoothItem -> Bluetooth
        null -> null
    }
}
