/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.base.BaseViewModel
import com.kolibree.android.app.ui.DynamicCardListConfiguration
import com.kolibree.android.app.ui.card.DynamicCardBindingModel
import com.kolibree.android.app.ui.card.DynamicCardViewModel
import com.kolibree.android.headspace.R
import com.kolibree.android.partnerships.data.DiscountCodeNotAvailableException
import com.kolibree.android.partnerships.domain.DisablePartnershipUseCase
import com.kolibree.android.partnerships.domain.PartnershipStatusUseCase
import com.kolibree.android.partnerships.domain.UnlockPartnershipUseCase
import com.kolibree.android.partnerships.domain.model.Partner
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.InProgress
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Inactive
import com.kolibree.android.partnerships.headspace.domain.HeadspacePartnershipStatus.Unlocked
import com.kolibree.android.utils.CopyToClipboardUseCase
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@VisibleForApp
class HeadspaceTrialCardViewModel(
    initialViewState: HeadspaceTrialCardViewState,
    private val statusUseCase: PartnershipStatusUseCase,
    private val unlockUseCase: UnlockPartnershipUseCase,
    private val navigator: HeadspaceTrialNavigator,
    private val clipboardUseCase: CopyToClipboardUseCase,
    private val disableUseCase: DisablePartnershipUseCase
) : DynamicCardViewModel<
    HeadspaceTrialCardViewState,
    HeadspaceTrialCardInteraction,
    HeadspaceTrialCardBindingModel>(initialViewState),
    HeadspaceTrialCardInteraction {

    override val interaction = this

    override fun interactsWith(bindingModel: DynamicCardBindingModel) =
        bindingModel is HeadspaceTrialCardBindingModel

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        disposeOnDestroy {
            statusUseCase.getPartnershipStatusStream(Partner.HEADSPACE)
                .cast(HeadspacePartnershipStatus::class.java)
                .subscribe(
                    { newStatus -> onNewHeadspaceStatus(newStatus) },
                    Timber::e
                )
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        // TODO replace it with proper synchronisation
        refreshPartnershipData()
    }

    private fun refreshPartnershipData() {
        disposeOnPause { statusUseCase.refreshPartnershipData().subscribe({}, Timber::e) }
    }

    override fun onToggleDescriptionClick() {
        getViewState()?.apply {
            if (isDescriptionVisible) {
                HeadspaceTrialAnalytics.hideDescription()
            } else {
                HeadspaceTrialAnalytics.showDescription()
            }
        }

        updateViewState { copy(isDescriptionVisible = !isDescriptionVisible) }
    }

    private fun onNewHeadspaceStatus(status: HeadspacePartnershipStatus) {
        when (status) {
            is InProgress -> onInProgress(status)
            is Unlocked -> onUnlocked(status)
            is Inactive -> onInactive(status)
        }
    }

    private fun onInProgress(status: InProgress) {
        updateViewState { withInProgressStatus(status) }
    }

    private fun onUnlocked(status: Unlocked) {
        updateViewState { withUnlockedStatus(status) }
    }

    private fun onInactive(status: Inactive) {
        updateViewState { withInactiveStatus(status) }
    }

    override fun onCallToActionClicked() {
        getViewState()?.let { viewState ->
            if (viewState.isUnlockable) {
                unlockTrial()
            } else if (viewState.isUnlocked) {
                redeemCode(viewState)
            }
        }
    }

    private fun unlockTrial() {
        HeadspaceTrialAnalytics.unlock()

        disposeOnDestroy {
            unlockUseCase.unlockCompletable(Partner.HEADSPACE)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        // no-op. Changes will be picked up by statusStream
                        Timber.d("Unlock completed")
                    },
                    ::onUnlockError
                )
        }
    }

    private fun onUnlockError(throwable: Throwable) {
        Timber.e(throwable)

        if (throwable is DiscountCodeNotAvailableException) {
            navigator.showSnackbarError(R.string.headspace_card_unlock_error_no_codes)
        } else {
            navigator.showSomethingWentWrong()
        }
    }

    private fun fakeUnlockedState() {
        refreshPartnershipData()
    }

    private fun redeemCode(viewState: HeadspaceTrialCardViewState) {
        HeadspaceTrialAnalytics.visitHeadspace()

        viewState.redeemUrl?.let { url -> navigator.openUrl(url) }
    }

    override fun onTapToCopyClicked() {
        try {
            HeadspaceTrialAnalytics.copyCode()

            getViewState()?.discountCode?.let { discountCode ->
                clipboardUseCase.copy(
                    label = discountCode,
                    text = discountCode
                )
            }

            updateViewState { copy(copiedToClipboard = true) }
        } catch (e: Exception) {
            Timber.e(e, "Copy to clipboard failed")

            navigator.showSomethingWentWrong()
        }
    }

    override fun onCloseClicked() {
        HeadspaceTrialAnalytics.quit()

        navigator.confirmDismissCard(
            onConfirmAction = { onDismissConfirmed() },
            onCancelAction = { onCancelQuit() }
        )
    }

    private fun onCancelQuit() {
        HeadspaceTrialAnalytics.dismissQuit()
    }

    private fun onDismissConfirmed() {
        HeadspaceTrialAnalytics.confirmQuit()

        disposeOnDestroy {
            disableUseCase.disableCompletable(Partner.HEADSPACE)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        // no-op, changes will be picked up by refreshStream
                    },
                    ::onDismissError
                )
        }
    }

    private fun onDismissError(throwable: Throwable) {
        Timber.e(throwable)

        navigator.showSomethingWentWrong()
    }

    internal class Factory @Inject constructor(
        private val useCase: PartnershipStatusUseCase,
        private val unlockUseCase: UnlockPartnershipUseCase,
        private val disableUseCase: DisablePartnershipUseCase,
        private val navigator: HeadspaceTrialNavigator,
        private val clipboardUseCase: CopyToClipboardUseCase,
        private val dynamicCardListConfiguration: DynamicCardListConfiguration
    ) : BaseViewModel.Factory<HeadspaceTrialCardViewState>() {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HeadspaceTrialCardViewModel(
            viewState ?: HeadspaceTrialCardViewState.initial(
                position = dynamicCardListConfiguration
                    .getInitialCardPosition(HeadspaceTrialCardViewModel::class.java)
            ),
            statusUseCase = useCase,
            navigator = navigator,
            disableUseCase = disableUseCase,
            clipboardUseCase = clipboardUseCase,
            unlockUseCase = unlockUseCase
        ) as T
    }
}
