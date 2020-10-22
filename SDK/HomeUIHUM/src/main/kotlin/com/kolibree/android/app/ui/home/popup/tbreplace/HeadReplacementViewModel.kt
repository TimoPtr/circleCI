/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.popup.tbreplace

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.base.BaseViewState
import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.ui.home.HumHomeNavigator
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority
import com.kolibree.android.app.ui.home.tracker.HomeDisplayPriority.ReplaceHeadItem
import com.kolibree.android.app.ui.navigation.HomeScreenAction
import com.kolibree.android.app.ui.priority.DisplayPriorityItemUseCase
import com.kolibree.android.app.ui.toolbartoothbrush.SingleToothbrushConnected
import com.kolibree.android.app.ui.toolbartoothbrush.ToothbrushConnectionStateViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import org.threeten.bp.LocalDate
import timber.log.Timber

@VisibleForApp
class HeadReplacementViewModel(
    private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
    private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
    private val headReplacementUseCase: HeadReplacementUseCase,
    private val navigator: HumHomeNavigator
) : BaseViewModel<EmptyBaseViewState, HomeScreenAction>(EmptyBaseViewState) {

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        disposeOnPause(::monitorHeadReplacement)
    }

    private fun monitorHeadReplacement(): Disposable {
        return toothbrushConnectionStateViewModel.viewStateFlowable
            .map { it.state }.ofType(SingleToothbrushConnected::class.java)
            .flatMapCompletable { toothbrush ->
                headReplacementUseCase.isDisplayable(toothbrush.mac)
                    .flatMapCompletable { localDate ->
                        displayHeadReplace(toothbrush.mac, localDate)
                    }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({}, Timber::e)
    }

    /**
     * Submit the [ReplaceHeadItem] item to the Priority Queue and show the Dialog once it is ready
     * to be displayed
     */
    private fun displayHeadReplace(mac: String, replacementDate: LocalDate): Completable {
        return priorityItemUseCase.submitAndWaitFor(ReplaceHeadItem)
            .andThen(headReplacementUseCase.setReplaceHeadShown(mac, replacementDate))
            .observeOn(AndroidSchedulers.mainThread())
            .andThen(Completable.fromAction { navigator.showHeadReplacementDialog() })
    }

    @VisibleForApp
    class Factory @Inject constructor(
        private val priorityItemUseCase: DisplayPriorityItemUseCase<HomeDisplayPriority>,
        private val toothbrushConnectionStateViewModel: ToothbrushConnectionStateViewModel,
        private val headReplacementUseCase: HeadReplacementUseCase,
        private val navigator: HumHomeNavigator
    ) : BaseViewModel.Factory<BaseViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HeadReplacementViewModel(
                priorityItemUseCase,
                toothbrushConnectionStateViewModel,
                headReplacementUseCase,
                navigator
            ) as T
    }
}
