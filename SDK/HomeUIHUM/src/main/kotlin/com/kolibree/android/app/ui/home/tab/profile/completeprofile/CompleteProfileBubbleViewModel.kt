/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.profile.completeprofile

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.tracker.Analytics
import com.kolibree.databinding.livedata.LiveDataTransformations.mapNonNull
import com.kolibree.databinding.livedata.distinctUntilChanged
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

internal class CompleteProfileBubbleViewModel(
    initialViewState: CompleteProfileBubbleViewState,
    private val completeProfileBubbleUseCase: CompleteProfileBubbleUseCase
) : BaseViewModel<CompleteProfileBubbleViewState, HomeScreenAction>(initialViewState) {

    val profileBubbleVisible = mapNonNull<CompleteProfileBubbleViewState, Boolean>(
        viewStateLiveData,
        initialViewState.profileBubbleVisible
    ) { viewState -> viewState.profileBubbleVisible }.distinctUntilChanged()

    val profileBubbleProgress = mapNonNull<CompleteProfileBubbleViewState, Float>(
        viewStateLiveData,
        initialViewState.profileBubbleProgress
    ) { viewState -> viewState.profileBubbleProgress }.distinctUntilChanged()

    fun onProfileBubbleGotItClick() {
        Analytics.send(CompleteProfileBubbleAnalytics.close())
        completeProfileBubbleUseCase.suppressBubble()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        disposeOnStop(::subscribeToShowCompleteProfileBubbleStream)
        disposeOnStop(::subscribeToProfileCompletionPercentageStream)
    }

    private fun subscribeToShowCompleteProfileBubbleStream(): Disposable {
        return completeProfileBubbleUseCase.getShowCompleteProfileBubbleStream()
            .subscribeOn(Schedulers.io())
            .subscribe(::showCompleteProfileBubble, Timber::e)
    }

    private fun subscribeToProfileCompletionPercentageStream(): Disposable {
        return completeProfileBubbleUseCase.getProfileCompletionPercentageStream()
            .subscribeOn(Schedulers.io())
            .subscribe(::updateProfileCompletionPercentage, Timber::e)
    }

    private fun showCompleteProfileBubble(show: Boolean) {
        updateViewState {
            if (show && !profileBubbleVisible) {
                Analytics.send(CompleteProfileBubbleAnalytics.show())
            }
            copy(profileBubbleVisible = show)
        }
    }

    private fun updateProfileCompletionPercentage(completionPercentage: Int) {
        updateViewState { copy(profileCompletionPercentage = completionPercentage) }
    }

    class Factory @Inject constructor(
        private val completeProfileBubbleUseCase: CompleteProfileBubbleUseCase
    ) : BaseViewModel.Factory<CompleteProfileBubbleViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CompleteProfileBubbleViewModel(
                viewState ?: CompleteProfileBubbleViewState.initial(),
                completeProfileBubbleUseCase
            ) as T
    }
}
