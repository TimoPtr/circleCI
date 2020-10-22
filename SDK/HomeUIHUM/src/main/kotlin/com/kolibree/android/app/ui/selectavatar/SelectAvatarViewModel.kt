/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.selectavatar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.base.BaseAction
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.databinding.livedata.LiveDataTransformations.map
import com.kolibree.sdkws.core.AvatarCache
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

internal class SelectAvatarViewModel(
    initialViewState: SelectAvatarViewState,
    private val initialActionProvider: Provider<SelectAvatarAction?> = SelectAvatarActionInitialProvider(),
    private val currentProfileProvider: CurrentProfileProvider,
    private val avatarSelectedUseCase: AvatarSelectedUseCase,
    private val avatarCache: AvatarCache,
    private val imageCaptor: ImageCaptor,
    private val takeExternalStoragePictureContract: TakeExternalStoragePictureContract
) : BaseViewModel<SelectAvatarViewState, SelectAvatarAction>(
    initialViewState,
    initialAction = initialActionProvider
) {

    val avatarUrl: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.avatarUrl ?: ""
    }

    val profileName: LiveData<String> = map(viewStateLiveData) { viewState ->
        viewState?.profileName ?: ""
    }

    init {
        disposeOnCleared(::readProfileOnce)
    }

    private fun readProfileOnce(): Disposable {
        return currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .subscribe(
                ::onProfileLoaded,
                Timber::e
            )
    }

    private fun onProfileLoaded(profile: Profile) {
        updateViewState {
            copy(
                avatarUrl = avatarCache.getAvatarUrl(profile),
                profileName = profile.firstName
            )
        }
    }

    fun onTakePictureClicked() {
        pushAction(SelectAvatarAction.LaunchCameraAction)
    }

    fun onChooseFromGalleryClicked() {
        pushAction(SelectAvatarAction.ChooseFromGalleryAction)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PICK_PICTURE_REQUEST_CODE -> {
                onChooseFromGalleryResult(resultCode, data)

                sendDismissAction()
            }
            else -> {
                Timber.w("Received unexpected code $requestCode")
                // no-op
            }
        }
    }

    /**
     * Must be called from fragment's onCreate
     *
     * We can't hold a reference to the ActivityResultCaller or the VM will leak the fragment
     */
    fun prepareImageCapture(activityResultCaller: ActivityResultCaller) {
        imageCaptor.prepareCaptureBitmap(
            coroutineScope = viewModelScope,
            activityResultCaller = activityResultCaller,
            contract = takeExternalStoragePictureContract
        ) { bitmap ->
            bitmap?.let {
                avatarSelectedUseCase.onPictureTaken(bitmap)
            } ?: avatarSelectedUseCase.onTakePictureNotOk()

            sendDismissAction()
        }
    }

    fun acquireImage() {
        imageCaptor.captureBitmap()
    }

    private fun onChooseFromGalleryResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            onUriSelected(data?.data)
        } else {
            avatarSelectedUseCase.onChooseFromGalleryNotOk()
        }
    }

    private fun sendDismissAction() {
        if (isResumed()) {
            pushAction(SelectAvatarAction.DismissDialog)
        } else {
            actionProvider().action = SelectAvatarAction.DismissDialog
        }
    }

    private fun actionProvider(): SelectAvatarActionInitialProvider =
        initialActionProvider as SelectAvatarActionInitialProvider

    private fun onUriSelected(uri: Uri?) {
        if (uri != null) {
            avatarSelectedUseCase.onUriSelected(uri)
        }
    }

    class Factory @Inject constructor(
        private val currentProfileProvider: CurrentProfileProvider,
        private val avatarSelectedUseCase: AvatarSelectedUseCase,
        private val avatarCache: AvatarCache,
        private val imageCaptor: ImageCaptor,
        private val takeExternalStoragePictureContract: TakeExternalStoragePictureContract
    ) : BaseViewModel.Factory<SelectAvatarViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SelectAvatarViewModel(
                initialViewState = viewState
                    ?: SelectAvatarViewState.initial(),
                currentProfileProvider = currentProfileProvider,
                avatarSelectedUseCase = avatarSelectedUseCase,
                avatarCache = avatarCache,
                takeExternalStoragePictureContract = takeExternalStoragePictureContract,
                imageCaptor = imageCaptor
            ) as T
    }
}

@Parcelize
internal data class SelectAvatarViewState(
    val avatarUrl: String? = null,
    val profileName: String? = null
) : BaseViewState {
    companion object {
        fun initial() =
            SelectAvatarViewState()
    }
}

internal sealed class SelectAvatarAction : BaseAction {
    object LaunchCameraAction : SelectAvatarAction()
    object ChooseFromGalleryAction : SelectAvatarAction()
    object DismissDialog : SelectAvatarAction()
}

@VisibleForTesting
internal class SelectAvatarActionInitialProvider : Provider<SelectAvatarAction?> {
    var action: SelectAvatarAction? = null
    override fun get(): SelectAvatarAction? {
        return action.also {
            action = null
        }
    }
}
