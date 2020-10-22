/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.toolbar

import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.NO_MAC_ADDRESS
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.selectprofile.SelectProfileDialogUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionState
import com.kolibree.android.commons.AppConfiguration
import com.kolibree.android.shop.data.repo.CartRepository
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class HomeToolbarViewModel(
    initialState: HomeToolbarViewState,
    private val homeNavigator: HumHomeNavigator,
    private val cartRepository: CartRepository,
    private val currentProfileProvider: CurrentProfileProvider,
    private val selectProfileDialogUseCase: SelectProfileDialogUseCase
) : BaseViewModel<HomeToolbarViewState, HomeScreenAction>(initialState) {

    val toolbarProfilePickerEnabled: LiveData<Boolean> = mapNonNull(
        viewStateLiveData,
        initialState.toolbarProfilePickerEnabled
    ) { viewState -> viewState.toolbarProfilePickerEnabled }

    val toolbarIconResult = map(viewStateLiveData) { viewState ->
        viewState?.toolbarIconResult ?: ToolbarIconResult()
    }

    val productsInCart: LiveData<Int> = map(viewStateLiveData) { viewState ->
        viewState?.productsInCart ?: 0
    }

    val getCartCountVisibility: LiveData<Int> = map(viewStateLiveData) { viewState ->
        if (viewState?.productsInCart ?: 0 > 0) View.VISIBLE else View.GONE
    }

    val profileName: LiveData<String?> = map(viewStateLiveData) { viewState ->
        viewState?.profileName
    }

    var topOffset = map(viewStateLiveData) { viewState ->
        viewState?.topOffset
    }

    fun setTopOffset(topOffset: Int) {
        updateViewState { copy(topOffset = topOffset) }
    }

    fun onProfileNameClick() {
        disposeOnCleared {
            selectProfileDialogUseCase.showDialogAndHandleSelectedItem()
                .subscribeOn(Schedulers.io())
                .subscribe({ }, Timber::e)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop(::listenForCartUpdate)

        disposeOnStop(::listenForProfileChange)
    }

    private fun listenForCartUpdate() =
        cartRepository.getCartProductsCount()
            .subscribeOn(Schedulers.io())
            .subscribe(this::onNewCartProductsCount, Timber::e)

    private fun listenForProfileChange() =
        currentProfileProvider.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onCurrentProfileChanged, Timber::e)

    fun toolbarToothbrushIconClick() {
        ToolbarEventTracker.toothbrushMenu()
        when {
            isNoToothbrush(toolbarIconResult.value) -> homeNavigator.navigateToSetupToothbrushScreen()
            isToothbrushMacAddress() -> {
                homeNavigator.navigateToToothbrushScreen(toothbrushMacAddress())
            }
            else -> homeNavigator.navigateToMyToothbrushesScreen()
        }
    }

    fun toolbarCartIconClick() {
        ToolbarEventTracker.cart()
        homeNavigator.showCheckoutScreen()
    }

    @VisibleForTesting
    fun onNewCartProductsCount(count: Int) {
        updateViewState { copy(productsInCart = count) }
    }

    private fun toothbrushMacAddress(): String {
        return toolbarIconResult.value?.relatedMacAddress ?: NO_MAC_ADDRESS
    }

    @VisibleForTesting
    fun isToothbrushMacAddress(): Boolean {
        return toolbarIconResult.value?.relatedMacAddress?.isNotEmpty() ?: false
    }

    @VisibleForTesting
    fun isNoToothbrush(icon: ToolbarIconResult?) =
        icon?.let { it.toolbarIcon == ToolbarIcon.NoToothbrush } ?: true

    fun renderIcon(state: ToothbrushConnectionState) {
        val result = ToolbarIconMapper.map(state)
        updateViewState { copy(toolbarIconResult = result) }
    }

    private fun onCurrentProfileChanged(profile: Profile) {
        updateViewState { copy(profileName = profile.firstName) }
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val homeNavigator: HumHomeNavigator,
        private val appConfiguration: AppConfiguration,
        private val cartRepository: CartRepository,
        private val currentProfileProvider: CurrentProfileProvider,
        private val selectProfileDialogUseCase: SelectProfileDialogUseCase
    ) : BaseViewModel.Factory<HomeToolbarViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeToolbarViewModel(
                viewState ?: HomeToolbarViewState.initial(
                    toolbarProfilePickerEnabled = appConfiguration.isSelectProfileSupported
                ),
                homeNavigator,
                cartRepository,
                currentProfileProvider,
                selectProfileDialogUseCase
            ) as T
    }
}
